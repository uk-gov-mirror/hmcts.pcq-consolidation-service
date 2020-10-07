#!/bin/bash
binFolder=$(dirname "$0")

(${binFolder}/idam-create-caseworker.sh ccd-import ccd.docker.default@hmcts.net Pa55word11 Default CCD_Docker)

(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-manager test@gmail.com)
(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-junior pcq.junior.ccd@gmail.com)
(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-manager pcq.manager.ccd@gmail.com)
(${binFolder}/idam-create-caseworker.sh caseworker,caseworker-pcqtest,caseworker-pcqtest-pcqextractor pcq.extractor.ccd@gmail.com)

(${binFolder}/ccd-add-role.sh caseworker)
(${binFolder}/ccd-add-role.sh caseworker-pcqtest)

(${binFolder}/ccd-add-role.sh caseworker-pcqtest-junior)
(${binFolder}/ccd-add-role.sh caseworker-pcqtest-manager)

(${binFolder}/ccd-add-role.sh caseworker-pcqtest-pcqextractor)

