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

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import sbt.util.Logger
import sbt.{File, URL}

import java.io.{BufferedInputStream, IOException, InputStream}
import java.net.URI
import java.nio.file.attribute.{BasicFileAttributes, PosixFilePermission}
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.util

object ProtofetchBinary {

  val DEFAULT_VERSION = "0.1.1"

  def download(
      logger: Logger,
      system: SystemTuple,
      version: String,
      target: File
  ): File = {
    val suffix = system match {
      case SystemTuple(Platform.Linux, Architecture.Amd64) =>
        "x86_64-unknown-linux-musl"
      case SystemTuple(Platform.Linux, Architecture.AArch64) =>
        "aarch64-unknown-linux-musl"
      case SystemTuple(Platform.Mac, Architecture.Amd64) =>
        "x86_64-apple-darwin"
      case SystemTuple(Platform.Mac, Architecture.AArch64) =>
        "aarch64-apple-darwin"
      case SystemTuple(Platform.Windows, Architecture.Amd64) =>
        "x86_64-pc-windows-msvc"
      case SystemTuple(Platform.Windows, Architecture.AArch64) =>
        scala.sys.error("Unsupported system: AArch64 Windows")
    }

    val url = URI
      .create(
        s"https://github.com/coralogix/protofetch/releases/download/v$version/protofetch_$suffix.tar.gz"
      )
      .toURL

    logger.info(s"Downloading $url")

    val targetPath = target.toPath

    if (target.exists()) {
      delete(targetPath)
    }

    val stream = url.openStream()
    try extract(logger, stream, targetPath)
    finally stream.close()

    val binary = system.platform match {
      case Platform.Linux | Platform.Mac => "protofetch"
      case Platform.Windows              => "protofetch.exe"
    }

    targetPath.resolve("bin").resolve(binary).toFile
  }

  private def delete(path: Path): Unit = {
    Files.walkFileTree(
      path,
      new SimpleFileVisitor[Path] {
        override def visitFile(
            path: Path,
            attrs: BasicFileAttributes
        ): FileVisitResult = {
          Files.delete(path)
          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(
            path: Path,
            exc: IOException
        ): FileVisitResult = {
          Files.delete(path)
          FileVisitResult.CONTINUE
        }
      }
    )
  }

  private def extract(
      logger: Logger,
      input: InputStream,
      target: Path
  ): Unit = {
    val tar = new TarArchiveInputStream(
      new GzipCompressorInputStream(new BufferedInputStream(input))
    )

    while (tar.getNextEntry != null) {
      val entry     = tar.getCurrentEntry
      val extractTo = target.resolve(entry.getName)
      if (entry.isDirectory) {
        logger.debug(s"Creating $extractTo")
        Files.createDirectories(extractTo)
      } else {
        logger.debug(s"Extracting $extractTo")
        Files.createDirectories(extractTo.getParent)
        Files.copy(tar, extractTo)
        Files.setPosixFilePermissions(extractTo, toPermissions(entry.getMode))
      }
    }
  }

  private def toPermissions(mode: Int): java.util.Set[PosixFilePermission] = {
    val result =
      util.EnumSet.noneOf[PosixFilePermission](classOf[PosixFilePermission])

    var currentMode = mode
    for (permission <- PosixFilePermission.values.reverse) {
      if ((currentMode & 1) == 1)
        result.add(permission)
      currentMode = currentMode >> 1
    }

    result
  }
}
