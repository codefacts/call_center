site.reactjs.SummaryTable = React.createClass({
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
    render: function () {
        var $this = this;
        var data = $this.props.data;

        var total = $this.total(data);

        var width = 'auto';
        var cols = [
            {width: width, label: 'Grocery', name: 'grocery'},
            {width: width, label: 'Location', name: 'location'},
            {width: width, label: 'Point', name: 'posNo'},
            {width: width, label: 'Total', name: 'totalCount', apply: $this.makeLink},
            {width: width, label: 'Today', name: 'todayCount'},
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

                        return (
                            data.map(function (v) {
                                return (
                                    <tr key={Math.random()}>
                                        {cols.map(function (col) {
                                            return (<td key={col.name}
                                                        style={{borderBottom: 0}}>{col.apply(v[col.name], v)}</td>);
                                        })}
                                    </tr>
                                );
                            })
                        );
                    })()}
                    </tbody>

                    <tfoot>
                    <tr>
                        <td colSpan="3">
                            Total
                        </td>

                        <td>
                            {total.totalCount}
                        </td>
                        <td>
                            {total.todayCount}
                        </td>

                    </tr>
                    </tfoot>
                </table>
            </div>
        );
    },
    total: function (data) {
        if (data.length == 0) {
            return {
                totalCount: 0,
                todayCount: 0
            };
        }
        return data.reduce(function (p, c) {
            p = !!p ? p : {totalCount: 0, todayCount: 0};
            return {
                totalCount: p.totalCount + c.totalCount,
                todayCount: p.todayCount + c.todayCount
            }
        });
    },
    makeLink: function (val, v) {
        var $this = this;
        return (<span style={{cursor: 'pointer'}}
                      className={'text-primary'} onClick={function () {$this.gotoSummaryDetails(v)}}>{val}</span>);
    },
    gotoSummaryDetails: function (v) {
        console.log('before sum det')
        console.log(v)

        site.hash.goto('/summary-details', v);
    },
});