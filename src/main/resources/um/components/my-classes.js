function PaginationUtil(args) {
    var pg = {};

    var xx = {
        page: parseInt(args.page),
        size: parseInt(args.size),
        total: parseInt(args.total),
        navLength: parseInt(args.navLength),
        defaultSize: 20,
        pageCount: 0
    };

    for (var x in xx) {
        pg[x] = xx[x];
    }

    pg.size = pg.size < 1 ? pg.defaultSize : pg.size;
    pg.total = pg.total < 0 ? 0 : pg.total;
    pg.pageCount = parseInt(((pg.total % pg.size == 0) ? (pg.total / pg.size) : (pg.total / pg.size) + 1));
    pg.page = pg.page < 1 ? 1
        : pg.page > pg.pageCount ? pg.pageCount
        : pg.page;

    var $this;
    return $this = {
        pg: pg,
        getPage: function () {
            return pg.page;
        }.bind($this),

        getSize: function () {
            return pg.size;
        },

        hasPrev: function (p) {

            if (!p) return pg.page > 1;

            p = parseInt(p);
            if (!!p) {
                return p > 1;
            }
        },

        hasNext: function (p) {

            if (!p) return pg.page < pg.pageCount;

            p = parseInt(p);
            if (!!p) return p < pg.pageCount;
        },

        isFirst: function (p) {

            if (!p) return pg.page <= $this.first();

            p = parseInt(p);
            if (!!p) return p <= $this.first();
        }.bind($this),

        isLast: function (p) {

            if (!p) return pg.page >= $this.last();

            p = parseInt(p);
            if (!!p)return p >= $this.last();

        }.bind($this),

        isCurrentPage: function (aPage) {
            aPage = parseInt(aPage);
            return pg.page == parseInt(aPage);
        },

        next: function (pageTo) {

            if (!pageTo) return pg.page < pg.pageCount ? pg.page + 1 : pg.pageCount;

            pageTo = parseInt(pageTo);
            if (!!pageTo) return pageTo < pg.pageCount ? pageTo + 1 : pg.pageCount;
        },

        prev: function (pageFrom) {
            if (!pageFrom) return pg.page > 1 ? pg.page - 1 : 1;
            pageFrom = parseInt(pageFrom);
            if (!!pageFrom) return pageFrom > 1 ? pageFrom - 1 : 1;
        },

        first: function () {
            return 1;
        },

        last: function () {
            return pg.pageCount;
        },

        nav: function (navLength) {
            var builder = [];
            navLength = navLength || pg.navLength;
            navLength = navLength < 2 ? 2 : navLength;

            var nvLen = navLength > pg.pageCount ? pg.pageCount : navLength;
            if (nvLen == 1) {
                builder.push(pg.page);
                return builder;
            }
            var mid = (nvLen % 2 === 0) ? (nvLen / 2) : ((nvLen - 1) / 2);
            var trk = pg.page - mid;
            trk = trk < 1 ? 1 : trk;

            for (var i = 0; i < nvLen; i++) {
                var track = trk + i;
                if (track >= 1 && track <= pg.pageCount) builder.push(track);
            }
            return builder;
        }
    };

}