/*
 * Copyright 2024 Coralogix Ltd.
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

package com.coralogix.sbtprotofetch

import com.coralogix.sbtprotofetch.ProtofetchBinary.DEFAULT_VERSION
import munit.FunSuite
import sbt.util.Logger

import java.nio.file.Files

class ProtofetchBinaryTest extends FunSuite {

  def checkDownload(platform: Platform, architecture: Architecture)(implicit
      loc: munit.Location
  ): Unit = {
    test(s"can download binary for $platform ($architecture)") {
      val binary = ProtofetchBinary.download(
        Logger.Null,
        SystemTuple(platform, architecture),
        DEFAULT_VERSION,
        Files.createTempDirectory("sbt-protofetch").toFile
      )
      assert(binary.exists(), "protofetch binary does not exist")
      assert(binary.canExecute, "protofetch binary is not executable")
    }
  }

  checkDownload(Platform.Linux, Architecture.Amd64)
  checkDownload(Platform.Linux, Architecture.AArch64)
  checkDownload(Platform.Mac, Architecture.Amd64)
  checkDownload(Platform.Mac, Architecture.AArch64)
  checkDownload(Platform.Windows, Architecture.Amd64)

  test(s"can re-download") {
    val target = Files.createTempDirectory("sbt-protofetch").toFile
    val binary1 = ProtofetchBinary.download(
      Logger.Null,
      SystemTuple(Platform.Linux, Architecture.Amd64),
      DEFAULT_VERSION,
      target
    )
    binary1.setExecutable(false)
    assert(!binary1.canExecute)
    val binary2 = ProtofetchBinary.download(
      Logger.Null,
      SystemTuple(Platform.Linux, Architecture.Amd64),
      DEFAULT_VERSION,
      target
    )
    assertEquals(binary1, binary2)
    assert(binary2.canExecute)
  }
}
