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

import com.moandjiezana.toml.Toml
import sbt.File
import sbt.util.Logger

import scala.sys.process.Process
import scala.util.control.Exception.nonFatalCatch

object Protofetch {
  def getOutputDirectory(moduleFile: File): String = {
    nonFatalCatch
      .opt {
        val toml = new Toml().read(moduleFile)
        Option(toml.getString("proto_out_dir"))
      }
      .flatten
      .getOrElse("proto_src")
  }

  def fetch(
      cwd: File,
      logger: Logger,
      binary: File,
      moduleFile: File,
      lockFile: File,
      locked: Boolean
  ): Unit = {
    val command = Seq(
      Seq(
        binary.toString,
        "--module-location",
        moduleFile.toString,
        "--lockfile-location",
        lockFile.toString,
        "fetch"
      ),
      if (locked) Seq("--locked") else Nil
    ).flatten
    logger.debug(s"Running ${command.mkString(" ")}")
    val code = Process(command, cwd).run(logger).exitValue()
    if (code != 0) {
      scala.sys.error("Nonzero exit value: " + code)
    }
  }
}
