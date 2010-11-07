(ns leiningen.dx
 "A lein plugin to create android jars from a lein project. Similar to uberjars task, the dx task to create a master jar with the project's jar as well as all jars except android.jar in lib/"
  (:use [clojure.zip :only [xml-zip children]]
	[clojure.java.io :only [file]]
	[clojure.java.shell :only [sh]]
        [clojure.java.io :only [file copy]]
        [leiningen.core :only [abort]]
        [leiningen.clean :only [clean]]
        [leiningen.jar :only [get-jar-filename get-default-uberjar-name jar]]
        [leiningen.deps :only [deps]]
	[leiningen.classpath :only [get-classpath]])
  
)

(defmacro aif [ test & body]
  `(let [~'it ~test]
     (if  ~'it
	 ~@body)))
       
(defn default-dxjar-name [project]
  (str (:name project) "-android.jar"))

(defn get-dx-includes [project]
  "get everything except android.jar"
  (let [libdir (str (:jar-dir project) "/lib")]
    (map #(str "lib/" %) (filter (complement #(= "android.jar" %))
	    (filter #(.endsWith  % ".jar")
		    (.list (file libdir)))))))


(defn- classpath [project]
  (apply str (interpose
	      java.io.File/pathSeparatorChar
	      (apply conj
	      (get-classpath project)
	      (get-android-classpath project)))))

(defn get-dx-cmdline [project filename jarname]
  (let [tldir
	(aif (:android-platform-tooldir project)
	  (str it  "/") "")]
      (apply vector
	     (str tldir "dx")
	     "--dex"
	     (str "--output=" filename)
	     jarname
	     (get-dx-includes project))))

(defn dx [project]
  (doto project clean deps)
  (if (jar project)
    (let [filename (get-jar-filename project (default-dxjar-name project))
	  jarname (get-jar-filename project)
	  cmd (get-dx-cmdline project filename jarname)]
      (println "Calling dex.\n" (apply str (interpose " " cmd)))
      (let [r (apply sh cmd)]
	(aif (= 0 (:exit r))
	  (do (println (:out r) "\n Dalvik jar generated;" filename)  it)
	  (do (println "Dex operation failed") it))))))
			  
      ;; run dx here to generate the filename out.
      ;; (let [ret (try (sh (get-dx-cmdline project filename jarname))
      ;; 	   (catch java.lang.Exception e
      ;; 	     (println (str "Failed to compile: " e))))]
      ;; 	(println (:out ret))))))
	






