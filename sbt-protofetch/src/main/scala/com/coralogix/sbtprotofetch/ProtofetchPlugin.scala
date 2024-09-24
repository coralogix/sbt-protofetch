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

import sbt.*
import sbt.Keys.*
import sbt.util.CacheImplicits.*
import sbt.util.{FilesInfo, ModifiedFileInfo}

object ProtofetchPlugin extends AutoPlugin {

  object autoImport extends Keys

  import autoImport.*

  override lazy val globalSettings: Seq[Setting[_]] = Seq(
    protofetchVersion := ProtofetchBinary.DEFAULT_VERSION,
    protofetchBinary := {
      val s       = streams.value
      val version = protofetchVersion.value
      val system  = SystemTuple.detect()
      val target =
        s.cacheDirectory / "sbt-protofetch" / version / system.cacheKey

      val tracked = Tracked.lastOutput(s.cacheStoreFactory.make("output"))({
        (_: Unit, last: Option[(File, String, String)]) =>
          last
            .filter { case (binary, lastSystem, lastVersion) =>
              binary.exists() &&
              lastSystem == system.cacheKey &&
              lastVersion == version
            }
            .getOrElse {
              val binary =
                ProtofetchBinary.download(s.log, system, version, target)
              (binary, system.cacheKey, version)
            }
      })
      tracked(())._1
    }
  )

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    protofetchModuleFile := baseDirectory.value / "protofetch.toml",
    protofetchLockFile := protofetchModuleFile.value.getParentFile / "protofetch.lock",
    protofetchOutputDirectory := {
      val moduleFile = protofetchModuleFile.value
      moduleFile.getParentFile / Protofetch.getOutputDirectory(moduleFile)
    },
    protofetchFetch / insideCI := insideCI.value,
    protofetchFetch := {
      type Inputs  = (ModifiedFileInfo, HashFileInfo, Boolean)
      type Outputs = (HashFileInfo, FilesInfo[ModifiedFileInfo])

      val s              = streams.value
      val binaryFileInfo = FileInfo.lastModified(protofetchBinary.value)
      val moduleFileInfo = FileInfo.hash(protofetchModuleFile.value)
      val lockFile       = protofetchLockFile.value
      val locked         = (protofetchFetch / insideCI).value

      val tracked = Tracked.outputChangedW(s.cacheStoreFactory.make("fetch"))({
        (changed, _: (Inputs, Outputs)) =>
          if (changed) {
            Protofetch.fetch(
              baseDirectory.value,
              s.log,
              binaryFileInfo.file,
              moduleFileInfo.file,
              lockFile,
              locked
            )
          }
      })

      tracked({ () =>
        // We use pre-calculated values for the inputs
        val inputs = (binaryFileInfo, moduleFileInfo, locked)
        // And calculate fresh ones for the outputs
        val outputs = (
          FileInfo.hash(lockFile),
          FileInfo.lastModified(
            protofetchOutputDirectory.value.allPaths.get().toSet
          )
        )
        (inputs, outputs)
      })
    }
  )

}
