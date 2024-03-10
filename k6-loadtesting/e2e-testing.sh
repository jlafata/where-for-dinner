#!/usr/bin/env bash

#k6 run --insecure-skip-tls-verify --out influxdb=http://10.214.178.197:8086/k6 e2e-test.js                -u 60 -d 30m -i 50000
k6 run --insecure-skip-tls-verify --out influxdb=http://10.214.178.197:8086/k6 e2e-test.js                -u 100 -d 120m -i 36000
