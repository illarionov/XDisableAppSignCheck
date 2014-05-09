/*
 * Copyright (C) 2014 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru0xdc.xposed.disableappsignsheck;

import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!"android".equals(loadPackageParam.packageName)) return;

        XposedHelpers.findAndHookMethod("com.android.server.pm.PackageManagerService",
                loadPackageParam.classLoader,
                "compareSignatures",
                Signature[].class, Signature[].class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Object o = param.getResult();
                        if (! (o instanceof  Integer)) {
                            XposedBridge.log("compareSignatures() result is not integer");
                            return;
                        }
                        if ((Integer)o == PackageManager.SIGNATURE_NO_MATCH) {
                            Signature[] p1 = (Signature[])param.args[0];
                            Signature[] p2 = (Signature[])param.args[1];
                            if (p1 == null) p1 = new Signature[0];
                            if (p2 == null) p2 = new Signature[0];

                            String p1s[] = new String[p1.length];
                            for (int i =0; i<p1.length; ++i) p1s[i] = p1[i].toCharsString();

                            String p2s[] = new String[p2.length];
                            for (int i =0; i<p2.length; ++i) p2s[i] = p2[i].toCharsString();

                            XposedBridge.log(String.format("compareSignatures() was SIGNATURE_NO_MATCH. s1: %s s2: %s",
                                    Arrays.toString(p1s),
                                    Arrays.toString(p2s)
                                    ));

                            param.setResult(PackageManager.SIGNATURE_MATCH);
                        }
                    }
                }
        );
    }
}
