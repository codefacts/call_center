window.Pagination = React.createClass({
    getDefaultProps: function () {
        return {
            page: 1,
            size: 100,
            total: 0,
            navLength: 5,
            onPageRequest: function (e) {
                console.log(e)
            },
            style: {
                marginBottom: '10px',
                marginTop: '10px'
            }
        };
    },

    getInitialState: function () {
        return {};
    },

    onPageClick: function (e) {
        this.handleRequest(e);
    },

    onPrevClick: function (e) {
        this.handleRequest(e);
    },

    onNextClick: function (e) {
        this.handleRequest(e);
    },

    handleRequest: function (e) {
        this.props.onPageRequest($(e.target).attr("data-page"), this.props.size);
    },

    render: function () {
        var _self = this;

        var xxx = {
            page: parseInt(this.props.page),
            size: parseInt(this.props.size),
            total: parseInt(this.props.total),
            navLength: parseInt(this.props.navLength)
        };

        var pu = PaginationUtil(xxx);
        console.log(pu);

        var nav = pu.nav();
        var list = nav.map(function (p) {
            return (
                <li key={p} className={pu.isCurrentPage(p) ? "active" : ""}>
                    <span data-page={p} onClick={_self.onPageClick}>{p}</span>
                </li>
            );
        });

        return (

            <nav>
                <ul className="pagination pagination-sm" style={this.props.style}>
                    <li style={_self.isVisible(nav, 1) ? {display: 'none'} : {}}>
                        <span data-page={pu.first()} onClick={this.onPageClick}>{pu.first()}</span>
                    </li>
                    <li style={_self.isVisible(nav, 1) ? {display: 'none'} : {}}>
                        <span>...</span>
                    </li>
                    <li className={!pu.hasPrev() ? "disabled" : ""}>
                        <span data-page={pu.prev()} onClick={this.onPrevClick}>&laquo;</span>
                    </li>
                    {list}
                    <li className={!pu.hasNext() ? "disabled" : ""}>
                        <span data-page={pu.next()} onClick={this.onNextClick}>&raquo;</span>
                    </li>
                    <li style={_self.isVisible(nav, pu.last()) ? {display: 'none'} : {}}>
                        <span>...</span>
                    </li>
                    <li style={_self.isVisible(nav, pu.last()) ? {display: 'none'} : {}}>
                        <span data-page={pu.last()} onClick={this.onPageClick}>{pu.last()}</span>
                    </li>
                </ul>
            </nav>
        );
    },
    isVisible: function (nav, page) {
        return nav.indexOf(page) !== -1;
    }
})
;