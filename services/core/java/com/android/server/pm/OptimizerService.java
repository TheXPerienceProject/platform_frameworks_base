// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 XPerience Project

package com.android.server.pm;

import android.content.Context;
import android.os.CancellationSignal;

import com.android.server.LocalManagerRegistry;
import com.android.server.art.ArtManagerLocal;
import com.android.server.art.ReasonMapping;
import com.android.server.art.model.DexoptParams;
import com.android.server.art.model.DexoptResult;

import mx.xperience.optimizer.IOptimizerService;

/**
 * Internal system service (“optimizer”) that allows privileged apps
 * to trigger ART compilation (dexopt) on installed packages.
 *
 * <p>Starting with Android 14, the dexopt implementation was moved from
 * {@code IPackageManager} to the ART Service ({@link ArtManagerLocal}), a
 * “local” API that can only be invoked within the {@code system_server} process. This
 * service acts as a bridge: it exposes {@link ArtManagerLocal#dexoptPackage}
 * via Binder so that system apps (such as XPerienceOptimizer)
 * can invoke it without relying on shell commands restricted to root/shell
 * (see {@code ArtShellCommand#enforceRootOrShell}).
 *
 * <p>Registered in {@link com.android.server.SystemServer} under the
 * service name {@code “optimizer”}. Access is protected by the
 * signature permission {@code mx.xperience.optimizer.permission.COMPILE_PACKAGES}.
 */
public class OptimizerService extends IOptimizerService.Stub {
    /** Context of {@code system_server}, used to validate the caller's permission. */
    private final Context mContext;
    private final PackageManagerLocal mPackageManagerLocal;

    /**
     * @param context system context (usually {@code mSystemContext}
     *                passed from {@link com.android.server.SystemServer})
     * @param pmLocal instance of {@link PackageManagerLocal} obtained via
     *                {@link LocalManagerRegistry}
     */
    public OptimizerService(Context context, PackageManagerLocal pmLocal) {
        mContext = context;
        mPackageManagerLocal = pmLocal;
    }

    /**
     * Forces the recompilation (dexopt) of an installed package using ART Service.
     *
     * @param packageName    name of the package to compile (e.g., “com.example.app”)
     * @param compilerFilter ART compilation filter, e.g., {@code “speed-profile”}
     *                       (adaptive AOT based on usage profile) or {@code “speed”}
     *                       (full AOT, ignores profiles)
     * @return {@code true} if the compilation was performed or skipped because it
     *         was already up to date ({@link DexoptResult#DEXOPT_PERFORMED} or
     *         {@link DexoptResult#DEXOPT_SKIPPED}); {@code false} if ART
     *         Service is unavailable or the final result was a failure.
     * @throws SecurityException if the caller does not have the
     *         {@code mx.xperience.optimizer.permission.COMPILE_PACKAGES} permission
     */
    @Override
    public boolean compilePackage(String packageName, String compilerFilter) {
        mContext.enforceCallingPermission(
                "mx.xperience.optimizer.permission.COMPILE_PACKAGES", null);

        ArtManagerLocal art = LocalManagerRegistry.getManager(ArtManagerLocal.class);
        if (art == null) return false;

        try (var snapshot = mPackageManagerLocal.withFilteredSnapshot()) {
            DexoptParams params = new DexoptParams.Builder(ReasonMapping.REASON_CMDLINE)
                    .setCompilerFilter(compilerFilter)
                    .setFlags(com.android.server.art.model.ArtFlags.FLAG_FORCE,
                              com.android.server.art.model.ArtFlags.FLAG_FORCE)
                    .build();
            DexoptResult result = art.dexoptPackage(
                    snapshot, packageName, params, new CancellationSignal());
            return result.getFinalStatus() == DexoptResult.DEXOPT_PERFORMED
                    || result.getFinalStatus() == DexoptResult.DEXOPT_SKIPPED;
        }
    }
}
