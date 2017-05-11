/*
* Copyright 2017 John Ahlroos
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.devsoap.plugin.tasks

import com.devsoap.plugin.Util
import com.devsoap.plugin.servers.ApplicationServer
import com.devsoap.plugin.configuration.ApplicationServerConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.TaskAction

/**
 * Runs the application on a application server
 *
 * @author John Ahlroos
 */
class RunTask extends DefaultTask {

    static final String NAME = 'vaadinRun'

    def server

    @Option(option = 'stopAfterStart', description = 'Should the server stop after starting')
    def boolean stopAfterStarting = false

    @Option(option = 'nobrowser', description = 'Do not open browser after server has started')
    def boolean nobrowser = false

    def cleanupThread = new Thread({
        if ( server ) {
            server.terminate()
            server = null
        }

        try {
            Runtime.getRuntime().removeShutdownHook(cleanupThread)
        } catch (IllegalStateException e) {
            // Shutdown of the JVM in progress already, we don't need to remove the hook it will be removed by the JVM
            project.logger.debug('Shutdownhook could not be removed. This can be ignored.', e)
        }
    })

    public RunTask() {
        dependsOn(CompileWidgetsetTask.NAME)
        dependsOn(CompileThemeTask.NAME)
        description = 'Runs the Vaadin application'
        Runtime.getRuntime().addShutdownHook(cleanupThread)
    }

    @TaskAction
    public void run() {
        def configuration = Util.findOrCreateExtension(project, ApplicationServerConfiguration)
        if ( nobrowser ) {
            configuration.openInBrowser = false
        }
        server = ApplicationServer.get(project, [], configuration)
        server.startAndBlock(stopAfterStarting)
    }
}