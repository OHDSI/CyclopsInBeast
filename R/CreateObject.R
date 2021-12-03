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


#' @export
createCyclopsInBeastLikelihood <- function(cyclopsFit) {
  stopifnot(class(cyclopsFit) == "cyclopsFit")

  rJava::.jcall("org/ohdsi/cyclops/CyclopsJniWrapper", "V", "addLibrary",
                getObjectName(libname, "CyclopsInBeast"))
  rJava::.jcall("org/ohdsi/cyclops/CyclopsJniWrapper", "V", "addLibrary",
                getObjectName(libname, "Cyclops"))

  dataPtr <- xptr::xptr_address(cyclopsFit$cyclopsData$cyclopsDataPtr)
  fitPtr <- xptr::xptr_address(cyclopsFit$interface)

  parameter <- rJava::.jnew("dr.inference.model.Parameter$Default", "p", as.integer(length(coef(cyclopsFit))))

  javaCyclops <- rJava::.jnew("org.ohdsi.cyclops.CyclopsLikelihood", "test", dataPtr, fitPtr,
                              rJava::.jcast(parameter, "dr.inference.model.Parameter"))

  return(javaCyclops)
}

#' @export
simulateBayesianAnalysis <- function(cyclopsFit,
                                     chainLength = 1100000,
                                     burnIn = 1e+05,
                                     subSampleFrequency = 100,
                                     priorMean = 0,
                                     priorSd = 2,
                                     seed = 1) {
  if (!supportsJava8()) {
    stop("Java 8 or higher is required")
  }

  if (isRmdCheck() && !isUnitTest()) {
    inform(paste("Function is executed as an example in R check:",
                 "Reducing chainLength and burnIn to reduce compute time.",
                 "Result may be unreliable"))
    chainLength <- 110000
    burnIn <- 10000
  }

  javaCyclops <- createCyclopsInBeastLikelihood(cyclopsFit)

  inform("Performing MCMC. This may take a while")

  # prior <- rJava::.jnew("org.ohdsi.metaAnalysis.HalfNormalOnStdDevPrior", 0, as.numeric(priorSd[2]))

  analysis <- rJava::.jnew("org.ohdsi.methods.CyclopsAnalysis",
                           javaCyclops,
                           as.numeric(priorMean),
                           as.numeric(priorSd))

  runner <- rJava::.jnew("org.ohdsi.methods.Runner",
                         rJava::.jcast(analysis, "org.ohdsi.methods.Analysis"),
                         as.integer(chainLength),
                         as.integer(burnIn),
                         as.integer(subSampleFrequency),
                         as.numeric(seed))

  runner$setConsoleWidth(getOption("width"))
  runner$run()

  runner$processSamples() # TODO Comment out at some point

  # parameterNames <- analysis$getParameterNames()
  # trace <- metaAnalysis$getTrace(as.integer(3))
  # traces <- matrix(ncol = length(parameterNames) - 2, nrow = length(trace))
  # traces[, 1] <- trace
  # for (i in 4:length(parameterNames)) {
  #   trace <- metaAnalysis$getTrace(as.integer(i))
  #   traces[, i - 2] <- trace
  # }
  # hdiMu <- HDInterval::hdi(traces[, 1], credMass = 1 - alpha)
  # hdiTau <- HDInterval::hdi(traces[, 2], credMass = 1 - alpha)
  # mu <- mean(traces[, 1])

  return(runner)
}


getObjectName <- function(libname, pkgname) {
  libs <- "libs"
  if (nchar(.Platform$r_arch)) {
    lib <- file.path("libs", .Platform$r_arch)
  }
  lib <- system.file(libs, paste(pkgname, .Platform$dynlib.ext,
                                 sep = ""), package = pkgname)
  return(lib)
}

isRmdCheck <- function() {
  return(Sys.getenv("_R_CHECK_PACKAGE_NAME_", "") != "")
}

isUnitTest <- function() {
  return(tolower(Sys.getenv("TESTTHAT", "")) == "true")

}

