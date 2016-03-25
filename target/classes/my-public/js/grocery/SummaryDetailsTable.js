site.reactjs.SummaryDetailsTable = React.createClass({
    getDefaultProps: function () {
        return {
            data: [],
            onInit: function () {
            }
        };
    },
    getInitialState: function () {
        return {
            __render: true
        };
    },
    componentDidMount: function () {
        var $this = this;
        $this.props.onInit($this);
    },
    componentWillUnmount: function () {
        var $this = this;
    },
    onWindowResize: function () {
        var $this = this;
    },
    render: function () {
        var $this = this;
        var data = $this.props.data;

        width = width / 5;

        var cols = [
            {width: width + 'px', label: 'Grocery', name: 'grocery'},
            {width: width + 'px', label: 'Location', name: 'location'},
            {width: width + 'px', label: 'Poin', name: 'posNo'},
            {width: width + 'px', label: 'Date', name: 'date', apply: $this.formatDate},
            {width: width + 'px', label: 'Total', name: 'totalCount'},
        ].map(function (col) {
                col.apply = !col.apply ? function (s) {
                    return s
                } : col.apply;
                return col;
            });

        return (
            <div className="table-responsive TablePrimary"
                 style={{border: '1px solid #ddd'}}>

                <table className="table table-stripped table-bordered table-hover MainTable"
                       style={{border: 0, borderTop: '1px solid #ddd'}}>

                    <thead>

                    <tr>
                        {cols.map(function (col) {
                            return (<th key={col.name}
                                        style={{borderBottom: 0}}>{col.label}</th>);
                        })}
                    </tr>
                    </thead>
                    <tbody className="MainTableBody">
                    {(function () {
                        var i = 0;
                        return (
                            data.map(function (v) {
                                return (
                                    <tr key={Math.random()}>
                                        {cols.map(function (col) {
                                            return (<td key={col.name}
                                                        style={{borderBottom: 0}}>{col.apply(v[col.name])}</td>);
                                        })}
                                    </tr>
                                );
                            })
                        );
                    })()}
                    </tbody>
                </table>
            </div>
        );
    },
    formatDate: function (date) {
        return formatDate(new Date(date));
    },
});