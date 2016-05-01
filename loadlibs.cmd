call mvn dependency:copy-dependencies -DincludeScope=compile -DoutputDirectory=lib -Pdep-logback
call mvn dependency:copy-dependencies -DexcludeScope=compile -DoutputDirectory=testlib
