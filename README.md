# hoplon-date-time-picker
A date and time picker build with hoplon.

## State
Currently this is in very early development. I have not build this into a jar
to add to Maven. I will do this in the future.

## Usage

### See the demo
Clone the repo, enter that directory and then run boot dev to start build the
project and run a local webserver.

``` shell
git clone https://github.com/5slight/hoplon-date-time-picker.git
cd hoplon-date-time-picker
boot dev
```

When this has finished, navigate to http://localhost:8000 in your web browser.

### In a project
Currently I wouldn't recommend doing this for anything important.
BE PREPARED FOR CHANGES!

Copy the directory `src/datepicker` to the source path of your own project then
you can require `datepicker.core` in your project.

``` clojure
(:require [datepicker.core :as dp])
```

For a date picker:
``` clojure
(let [state (cell "")]
    (dp/date-picker :state state))
```

For a time picker:
``` clojure
(let [state (cell "")]
    (dp/time-picker :state state))
```
