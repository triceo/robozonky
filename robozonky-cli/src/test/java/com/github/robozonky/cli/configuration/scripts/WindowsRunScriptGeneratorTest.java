/*
 * Copyright 2020 The RoboZonky Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robozonky.cli.configuration.scripts;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class WindowsRunScriptGeneratorTest {

    @Test
    void run() throws IOException {
        Path installFolder = Files.createTempDirectory("robozonky-install");
        Path robozonkyCli = Files.createTempFile(installFolder, "robozonky-", ".cli");
        Path distFolder = Files.createTempDirectory(installFolder, "dist");
        RunScriptGenerator generator = RunScriptGenerator.forWindows(distFolder.toFile(), robozonkyCli.toFile());
        assertThat(generator.getChildRunScript())
            .hasName("robozonky.bat");
        assertThat(generator.getRootFolder()
            .toPath())
                .isEqualTo(installFolder);
        File result = generator.apply(Arrays.asList("-a x", "-b"));
        String contents = Files.readString(result.toPath());
        String expected = "set \"JAVA_OPTS=%JAVA_OPTS% -a x -b\"\r\n" +
                distFolder + "\\robozonky.bat" + " @" + robozonkyCli;
        // toCharArray() is a hack to make this pass on Windows. The actual reason for failing is not know.
        assertThat(contents.toCharArray())
            .isEqualTo(expected.toCharArray());
    }

}
