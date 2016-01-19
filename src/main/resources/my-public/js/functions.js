function copy(v) {
    if (v === undefined) return v;
    return JSON.parse(JSON.stringify(v))
}

function merge(object1, object2) {
    var obj = {};
    for (var x in object1) {
        obj[x] = object1[x];
    }
    for (var x in object2) {
        obj[x] = obj[x] || object2[x];
    }
    return obj;
}