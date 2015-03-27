/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che.test.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.dcm4che.test.annotations.TestLocalConfig;
import org.dcm4che.test.annotations.TestParamDefaults;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Hesham elbadawi <bsdreko@gmail.com>
 * @author Roman K
 */

public class TestParametersRule implements TestRule {


    private BasicTest parametrizedTest;

    public TestParametersRule(BasicTest basicTest) {
        this.parametrizedTest = basicTest;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Method method = description.getTestClass().getMethod(description.getMethodName());
                getInstance().clearParams();
                for(Annotation anno: method.getAnnotations()) {
                    Class annoType = anno.annotationType();
                    getInstance().addParam(annoType.getSimpleName(),method.getAnnotation(annoType));
                }
                TestLocalConfig cnf = description.getTestClass().getAnnotation(TestLocalConfig.class);
                getInstance().addParam("defaultLocalConfig",cnf);
                TestParamDefaults props = description.getTestClass().getAnnotation(TestParamDefaults.class);
                getInstance().addParam("defaultParams", props);
                Method initMethod = null;
                    Method[] methods = description.getTestClass().getMethods();
                    for (Method m : methods) {
                        // Test any other things about it beyond the name...
                        if (m.getName().equals("init") ) {
                            initMethod=m;
                        }
                    }
                    getInstance().init((Class<? extends BasicTest>) description.getTestClass());
                
                base.evaluate();
            }
        };
    }

    public BasicTest getInstance() {
        return this.parametrizedTest;
    }
    
}

