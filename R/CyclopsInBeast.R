# Copyright 2021 Observational Health Data Sciences and Informatics
#
# This file is part of CyclopsInBeast
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#' @useDynLib CyclopsInBeast, .registration = TRUE
#' @keywords internal
"_PACKAGE"

#' @importFrom stats density
#' printCoefmat qchisq rexp rnorm
#' @importFrom rlang .data abort warn inform
#' @importFrom methods is
#' @import BeastJar
#'
NULL

.onLoad <- function(libname, pkgname) {
  cat("onLoad\n")
  beastLocation <- system.file("java/beast.jar", package = "BeastJar")
  rJava::.jpackage(pkgname, lib.loc = libname, nativeLibrary = TRUE, morePaths = beastLocation)
  # cat(libname)
  # cat("\n")
  # cat(pkgname)
  # cat("\n")
  rJava::.jcall("org/ohdsi/cyclops/CyclopsJniWrapper", "V", "addLibrary",
                .getObjectName(libname, "CyclopsInBeast"))
  rJava::.jcall("org/ohdsi/cyclops/CyclopsJniWrapper", "V", "addLibrary",
                .getObjectName(libname, "Cyclops"))
}

.getObjectName <- function(libname, pkgname) {
  libs <- "libs"
  if (nchar(.Platform$r_arch)) {
    lib <- file.path("libs", .Platform$r_arch)
  }
  lib <- system.file(libs, paste(pkgname, .Platform$dynlib.ext,
                                 sep = ""), package = pkgname)
  return(lib)
}








.onUnload <- function (libpath) {
  library.dynam.unload("CyclopsInBeast", libpath)
}


