/*
 * Copyright 2016 the original author or authors.
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
package org.gradle.internal.progress

import org.gradle.integtests.fixtures.AbstractConsoleFunctionalSpec
import org.gradle.test.fixtures.ConcurrentTestUtil

class BuildProgressLoggerIntegrationTest extends AbstractConsoleFunctionalSpec {
    def "buildSrc task progress is displayed in initialization phase"() {
        given:
        file("buildSrc/build.gradle") << """
            plugins { 
                id 'groovy' 
            }
            dependencies {
                testCompile 'junit:junit:4.12'
            }
"""
        file("buildSrc/src/test/groovy/org/gradle/integtest/BuildSrcTest.groovy") << """
            package org.gradle.integtest
            import org.junit.Test
            
            public class BuildSrcTest {
                @Test
                public void test() {
                    Thread.sleep(1000)
                    assert 1 == 1
                }
            }
        """

        buildFile << "task hello {}"

        when:
        def build = executer.withTasks("hello").start()

        then:
        ConcurrentTestUtil.poll(30) {
            // This asserts that we see incremental progress, not just 0% => 100%
            assert build.standardOutput.matches(/(?s).*-> \d{2}% INITIALIZING \[.*$/)
        }
    }
}
