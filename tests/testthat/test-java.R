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

test_that("Execute simple CyclopsInBeast command", {
  skip_if_not(supportsJava8())
  tmp <- rJava::.jnew("test.org.ohdsi.TestLoadJar")
  expect_equal(class(tmp)[1], "jobjRef")
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

  dataPtr <- xptr::xptr_address(cyclopsFit$cyclopsData$cyclopsDataPtr)
  fitPtr <- xptr::xptr_address(cyclopsFit$interface)

  parameter <- rJava::.jnew("dr.inference.model.Parameter$Default", "p", as.integer(length(coef(cyclopsFit))))

  javaCyclops <- rJava::.jnew("org.ohdsi.cyclops.CyclopsLikelihood", "test", dataPtr, fitPtr,
                              rJava::.jcast(parameter, "dr.inference.model.Parameter"))
})
