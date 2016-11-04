(ns initty.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [initty.core-test]))

(doo-tests 'initty.core-test)
