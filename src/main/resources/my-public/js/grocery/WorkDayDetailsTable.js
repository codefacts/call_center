site.reactjs.grocery.WorkDayDetailsTable = React.createClass({
    getDefaultProps: function () {
        return {
            data: [],
            onInit: function () {
            }
        };
    },
    getInitialState: function () {
        return this.interceptState({
            data: this.props.data,
            __render: true
        });
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
    shouldComponentUpdate: function (nextProps, nextState) {
        return this.state.__render !== nextState.__render;
    },
    render: function () {
        var $this = this;
        var data = $this.state.data;

        var cols = [
            {
                label: 'Signatue', name: 'signature', apply: function (name) {
                return (<img style={{width: '90px'}} src={'http://182.160.99.116:6060/app_data/images/' + name}/>)
            }
            },
            {width: '100px', label: 'ID', name: 'id'},
            {width: '150px', label: 'Name', name: 'name'},
            {width: '100px', label: 'Phone', name: 'phone'},
            {width: '100px', label: 'Age Group', name: 'ageGroup'},
            {width: '200px', label: 'Grocery', name: 'grocery'},

            {width: '200px', label: 'Location', name: 'location'},
            {width: '100px', label: 'Pos No', name: 'posNo'},
            {width: '150px', label: 'Address', name: 'address'},
            {
                width: '100px', label: 'Date', name: 'date', apply: function (date) {
                return formatDate(new Date(date));
            }
            },
            {
                width: '100px', label: 'Date And Time', name: 'fullDate', apply: function (date) {
                if (!date) return "";
                return formatTimeAmPm(new Date(date));
            }
            },
        ].map(function (col) {
                col.apply = !col.apply ? function (s) {
                    return s
                } : col.apply;
                return col;
            });

        return (
            <div className="table-responsive TablePrimary">

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
                                    <tr key={v.id}>
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
    updateData: function (data) {
        var $this = this;
        $this.setState(this.interceptState({data: data, __render: !$this.state.__render}));
    },
    interceptState: function (state) {
        state.data.forEach(function (v, i) {
            v.index = i;
        });
        return state;
    }
});