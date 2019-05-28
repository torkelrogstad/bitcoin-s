name := "bitcoin-s-core"

testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck,
                                      "-verbosity",
                                      "2")

coverageExcludedPackages := ".*gen"

coverageMinimum := 90

coverageFailOnMinimum := true
