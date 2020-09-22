# Graphique

[![Clojars Project](https://img.shields.io/clojars/v/dev.gpeltier/graphique.svg)](https://clojars.org/dev.gpeltier/graphique)

A Clojure utility that allows you to view and interact with graph data from
the REPL

## Usage

```clojure
(require '[graphique.core :as g])
(g/view some-data)
```

`some-data` is expected to be a vector or sequence of node maps which each
contain the keys `:id`, `:name`, and `:dependents`. `:id` is expected to
be a unique integer value, `:name` is expected to be a character string, and
the optional `:dependents` key should map to a sequence or vector of other
node IDs. The relationship where `b` is dependent on `a` is then displayed
as `a -> b`.

If your data does not conform to the format described above, you may supply a
remapping for any/all of the keys used. E.g.

```clojure
(g/view some-data {:id :my-id-key :name :my-name-key :dependents :my-deps})
```

## License

Copyright © 2020 Grant Peltier

Distributed under the GNU Lesser Public License v3.0
