function HashParams() {
    var handlers = [];
    var routs = [];

    function setHash(hash) {
        location.hash = hash;
    }

    function getHash() {
        return location.hash || "";
    }

    function serialize(params) {
        var strings = [];
        if (Array.isArray(params)) {
            params.forEach(function (p) {
                strings.push(encodeURIComponent(p.name) + "=" + encodeURIComponent(p.value));
            })
        } else {
            for (var x in params) {
                strings.push(encodeURIComponent(x) + "=" + encodeURIComponent(params[x]));
            }
        }
        return strings.join("&");
    }

    function path() {
        var hs = getHash();
        var idx = hs.indexOf("#");
        var idxHs = hs.indexOf("?");
        var s = hs.slice(idx < 0 ? 0 : (idx + 1), idxHs < 0 ? hs.length : idxHs);
        return s == "" ? "/" : s;
    }

    function setParams(params) {
        setHash(path() + "?" + serialize(params));
    }

    function getParams(hash) {
        hash = hash || getHash();
        var _params = {};
        if (hash.lastIndexOf("#") >= 0) {
            hash = hash.substr(hash.lastIndexOf("#") + 1);
        }
        if (hash.lastIndexOf("?") >= 0) {
            hash = hash.substr(hash.lastIndexOf("?") + 1);
        } else {
            return {};
        }

        if (hash.trim() == "") return _params;
        var splits = hash.split("&");
        for (var x in splits) {
            var part = splits[x] || "";
            var keyValuPair = part.split("=", 2);
            if (keyValuPair.length > 0) {
                _params[decodeURIComponent(keyValuPair[0])] = decodeURIComponent(keyValuPair[1]);
            }
        }
        return _params;
    }

    function on(uri, handler) {
        var $this = this;
        var parts = uri.split("/");
        var template = {originString: uri, parts: [], handler: handler};

        for (var x in parts) {
            var part = parts[x];
            if (part.trim() == "") continue;
            var p = {};
            if (part.startsWith(":")) {
                p.isVariable = true;
                p.value = part.slice(1, part.length);
            } else {
                p.isVariable = false;
                p.value = part;
            }
            template.parts.push(p);
        }
        routs.push(template);
        return $this;
    }

    function triggerHashChange() {
        $(window).trigger('hashchange');
    }

    var rs = {

        path: path,

        on: on,

        serialize: serialize,

        goto: function (uri, params) {
            uri = uri.startsWith("/") ? uri : "/" + uri;
            uri = uri + "?" + serialize(params || {});
            setHash(uri.endsWith("?") ? uri.slice(0, uri.length - 1) : uri);
        },
        reload: function () {
            triggerHashChange();
        },

        execute: function (uri, hash) {
            hash = !!hash ? hash : rs;
            uri = !!uri ? uri : hash.path();

            var parts = uri.split("/").filter(function (part) {
                return !!part;
            }).map(function (part) {
                return decodeURIComponent(part);
            });

            var maxScore = -1;
            var maxIndex = -1;
            var indx = 0;
            var templates = routs.filter(function (template) {
                return (template.parts.length == parts.length);
            }).filter(function (template) {
                for (var x in parts) {
                    if ((template.parts[x].isVariable == false) && (template.parts[x].value != parts[x])) {
                        return false;
                    }
                }
                return true;
            }).map(function (template) {
                template.score = 0;
                for (var x in parts) {
                    if (template.parts[x].isVariable == false && template.parts[x].value == parts[x]) {
                        var n = (parts.length - x);
                        template.score += ((n * n * n) + 3 * (n * n) + 2 * n) / 6;
                        if (template.score > maxScore) {
                            maxScore = template.score;
                            maxIndex = indx;
                        }
                    }
                }
                indx++;
                return template;
            }).filter(function (template) {
                return (template.score >= maxScore);
            }).forEach(function (template) {
                try {
                    var params = {};
                    for (var x in template.parts) {
                        var part = template.parts[x];
                        if (part.isVariable) {
                            params[part.value] = parts[x];
                        }
                    }
                    template.handler(hash, params);
                } catch (e) {
                    console.warn(e);
                }
            });
        },

        addHandler: function (_onChangeHandler) {
            if (!_onChangeHandler) return;
            handlers.push(_onChangeHandler);
        },
        removeHandler: function (_onChangeHandler) {
            if (!_onChangeHandler) return;
            delete handlers[handlers.indexOf(_onChangeHandler)];
            var hndls = [];
            for (var x in handlers) {
                if (!!handlers[x]) {
                    hndls.push(handlers[x]);
                }
            }
            handlers = hndls;
        },
        addAll: function (params) {
            if (!params) return;
            var pms = getParams();
            if (Array.isArray(params)) {
                params.forEach(function (e) {
                    pms[e.name] = pms.value;
                });
            } else {
                for (var x in params) {
                    pms[x] = params[x]
                }
            }
            setParams(pms);
        },
        removeAll: function (params) {
            if (!params) return;
            var pms = getParams();
            if (Array.isArray(params)) {
                for (var x in params) {
                    delete pms[params[x]];
                }
            } else {
                for (var x in params) {
                    delete pms[x];
                }
            }
            setParams(pms);
        },
        add(key, value) {
            var p = {};
            p[key] = value;
            rs.addAll(p);
        },
        remove: function (key) {
            var p = {};
            p[key] = null;
            rs.removeAll(p);
        },
        clear: function () {
            rs.params({});
        },
        params: function (params) {
            if (!!params) {
                setParams(params);
            } else {
                return getParams(params);
            }
        },
        hash: function (hash) {
            if (!!hash) {
                setHash(hash);
            } else {
                return getHash(hash);
            }
        },
        getHash: getHash,
        setHash: setHash,
        getParams: getParams,
        setParams: setParams
    };

    $(window).on('hashchange', function () {
        for (var x in handlers) {
            try {
                handlers[x](rs);
            } catch (e) {
                console.warn(e);
            }
        }
    });

    rs.addHandler(function (hash) {
        hash.execute(hash.path(), hash);
    });

    return rs;
}