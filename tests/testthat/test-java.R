library(testthat)
library(survival)

test_that("Java support", {
  skip_if_not(supportsJava8())
})

test_that("Find BEAST JAR", {
  fileName <- system.file("java/beast.jar", package = "BeastJar")
  expect_true(file.exists(fileName))
})

test_that("Execute simple BEAST command", {
  skip_if_not(supportsJava8())
  distribution <- rJava::.jnew("dr.math.distributions.NormalDistribution", 2, 1)
  expect_equal(distribution$getMean(), 2)
})

test_that("Transfer point to Cyclops c++ object", {
  skip_if_not(supportsJava8())

  test <- read.table(header=T, sep = ",", text = "
start, length, event, x1, x2
0, 4,  1,0,0
0, 3.5,1,2,0
0, 3,  0,0,1
0, 2.5,1,0,1
0, 2,  1,1,1
0, 1.5,0,1,0
0, 1,  1,1,0
")

  cyclopsData <- Cyclops::createCyclopsData(Surv(length, event) ~ x1 + x2, data = test,
                                            modelType = "cox")
  cyclopsFit <- Cyclops::fitCyclopsModel(cyclopsData)

  javaCyclops <- CyclopsInBeast:::createCyclopsInBeastLikelihood(cyclopsFit = cyclopsFit)

  # Initial log likelihood and parameter values
  expect_equal(javaCyclops$getLogLikelihood(), cyclopsFit$log_likelihood)
  beta <- javaCyclops$getParameter()
  expect_equivalent(beta$getParameterValues(), coef(cyclopsFit))
  expect_equal(javaCyclops$getCounts()[1], 0)

  # Store internal state
  javaCyclops$storeModelState()

  # Change beta
  beta$setParameterValue(as.integer(0), 0.5)
  expect_equal(beta$getParameterValue(as.integer(0)), 0.5)

  # Get new log likelihood
  expect_false(javaCyclops$getLogLikelihood() == cyclopsFit$log_likelihood)
  expect_equal(javaCyclops$getCounts()[1], 1)

  # Restore internal state
  javaCyclops$restoreModelState()

  expect_equal(javaCyclops$getLogLikelihood(), cyclopsFit$log_likelihood)
  expect_equal(javaCyclops$getCounts()[1], 1)
  expect_equivalent(beta$getParameterValues(), coef(cyclopsFit))

  # TODO Check gradient calculations!

})

test_that("Simple MCMC simulation", {
  skip_if_not(supportsJava8())

  test <- read.table(header=T, sep = ",", text = "
start, length, event, x1, x2
0, 4,  1,0,0
0, 3.5,1,2,0
0, 3,  0,0,1
0, 2.5,1,0,1
0, 2,  1,1,1
0, 1.5,0,1,0
0, 1,  1,1,0
")

  cyclopsData <- Cyclops::createCyclopsData(Surv(length, event) ~ x1 + x2, data = test,
                                    modelType = "cox")
  cyclopsFit <- Cyclops::fitCyclopsModel(cyclopsData)

  runner <-
    simulateBayesianAnalysis(cyclopsFit,
                           chainLength = 11000,
                           burnIn = 1000,
                           subSampleFrequency = 100,
                           priorMean = 0,
                           priorSd = 10,
                           seed = 666)

})

test_that("Compare exact simulation to likelihood-profile-use", {
  skip_if_not(supportsJava8())

  set.seed(666)
  population <- EvidenceSynthesis::simulatePopulations(EvidenceSynthesis::createSimulationSettings(nSites = 1))[[1]]
  cyclopsData <- Cyclops::createCyclopsData(Surv(time, y) ~ x + strata(stratumId),
                                            data = population,
                                            modelType = "cox")
  cyclopsFit <- Cyclops::fitCyclopsModel(cyclopsData)
  likelihoodProfile <- EvidenceSynthesis::approximateLikelihood(cyclopsFit, parameter = "x", approximation = "grid")

  # Exact simulation
  runner <-
    simulateBayesianAnalysis(cyclopsFit,
                             chainLength = 110000,
                             burnIn = 1e+04,
                             subSampleFrequency = 100,
                             priorMean = 1,
                             priorSd = 2,
                             seed = 666)
  trace <- runner$getTrace(as.integer(3))
  exactMean <- mean(trace)
  exactSd <- sd(trace)

  # Likelihood-profile simulation
  traces <- EvidenceSynthesis::approximateSimplePosterior(likelihoodProfile = likelihoodProfile,
                                                          chainLength = 110000,
                                                          burnIn = 1e+04,
                                                          subSampleFrequency = 100,
                                                          priorMean = 1,
                                                          priorSd = 2,
                                                          seed = 666)
  profileMean <- mean(traces$theta1)
  profileSd <- sd(traces$theta1)

  expect_equal(profileMean, exactMean, tolerance = 5e-2)  # TODO: Is the profile approximation that bad?
  expect_equal(profileSd, exactSd, tolerance = 5e-2)
})
