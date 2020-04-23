#!/bin/bash
binFolder=$(dirname "$0")

(${binFolder}/idam-create-caseworker.sh ccd-import ccd.docker.default@hmcts.net Pa55word11 Default CCD_Docker)

(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-manager auto.test.cnp@gmail.com)
(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-manager auto.test.cnp+fe@gmail.com)
(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-manager auto.ccd.fe@gmail.com)
(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-manager auto.test.cnp+fe.judge@gmail.com)
(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-junior auto.test.cnp+fejunior@gmail.com)
(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-manager test@gmail.com)
(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-pcqsystemuser pcqtest-pcquser+ccd@gmail.com)

(${binFolder}/ccd-add-role.sh caseworker)
(${binFolder}/ccd-add-role.sh caseworker-pcqtest)

(${binFolder}/ccd-add-role.sh caseworker-pcqtest-junior)
(${binFolder}/ccd-add-role.sh caseworker-pcqtest-manager)

(${binFolder}/ccd-add-role.sh caseworker-pcqtest-pcqsystemuser)

