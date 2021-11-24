library(testthat)

test_that("Java support", {
  skip_if_not(supportsJava8())
})

test_that("Find BEAST JAR", {
  fileName <- system.file("java/beast.jar", package = "BeastJar")
  expect_true(file.exists(fileName))
})
