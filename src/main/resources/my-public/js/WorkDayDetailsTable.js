site.reactjs.WorkDayDetailsTable = React.createClass({
    getDefaultProps: function () {
        return {
            data: [],
            onInit: function () {
            }
        };
    },
    getInitialState: function () {
        return this.interceptState({
            bodyHeight: this.bodyHeight(),
            data: this.props.data,
            __render: true
        });
    },
    componentDidMount: function () {
        var $this = this;
        $this.props.onInit($this);
        $(window).bind("resize", $this.onWindowResize);
    },
    componentWillUnmount: function () {
        var $this = this;
        $(window).unbind("resize", $this.onWindowResize);
    },
    onWindowResize: function () {
        var $this = this;
        $this.setState({bodyHeight: $this.bodyHeight(), __render: !$this.state.__render});
    },
    shouldComponentUpdate: function (nextProps, nextState) {
        return this.state.__render !== nextState.__render;
    },
    render: function () {
        var $this = this;
        var data = $this.state.data;

        return (
            <div className="table-responsive TablePrimary"
                 style={{border: '1px solid #ddd', height: $this.state.bodyHeight + 'px'}}>

                <table className="table table-stripped table-bordered table-hover MainTable"
                       style={{border: 0, borderTop: '1px solid #ddd'}}>

                    <thead>
                    <tr>
                        <th className="bg-primary" style={{width: '162px', borderBottom: 0}}>SMS ID</th>
                        <th className="bg-primary" style={{width: '162px', borderBottom: 0}}>Consumer</th>
                        <th className="bg-primary" style={{width: '125px', borderBottom: 0}}>Mobile</th>
                        <th className="bg-success" style={{width: '115px', borderBottom: 0}}>Brand</th>
                        <th className="bg-success" style={{width: '72px', borderBottom: 0}}>Age</th>
                        <th className="bg-danger" style={{width: '72px', borderBottom: 0}}>PTR</th>
                        <th className="bg-danger" style={{width: '72px', borderBottom: 0}}>SWP</th>
                        <th className="bg-info" style={{width: '72px', borderBottom: 0}}>REF.</th>
                        <th className="bg-info" style={{width: '72px', borderBottom: 0}}>G.A.</th>
                        <th className="bg-warning" style={{width: '72px', borderBottom: 0}}>Called</th>
                        <th className="bg-warning" style={{width: '92px', borderBottom: 0}}>Call Status</th>
                        <th className="bg-warning" style={{width: '120px', borderBottom: 0}}>M.C. Status</th>
                        <th className="bg-warning" style={{width: '92px', borderBottom: 0}}></th>
                    </tr>
                    </thead>
                    <tbody className="MainTableBody">
                    {(function () {
                        return (
                            data.map(function (v) {
                                return (
                                    <tr key={v.index}>
                                        <td className="bg-primary"
                                            style={{width: '162px', borderBottom: 0}}>{v.SMS_ID}</td>
                                        <td className="bg-primary"
                                            style={{width: '162px', borderBottom: 0}}>{v.Customer_Name}</td>
                                        <td className="bg-primary"
                                            style={{width: '125px', borderBottom: 0}}>{v.CONSUMER_MOBILE}</td>
                                        <td className="bg-success"
                                            style={{width: '115px', borderBottom: 0}}>{v.BRAND_NAME}</td>
                                        <td className="bg-success"
                                            style={{width: '72px', borderBottom: 0}}>{v['s.AGE']}</td>
                                        <td className="bg-danger"
                                            style={{width: '72px', borderBottom: 0}}>{!!v['s.PTR'] ? 'Yes' : 'No'}</td>
                                        <td className="bg-danger"
                                            style={{width: '72px', borderBottom: 0}}>{!!v['SWAP'] ? 'Yes' : 'No'}</td>
                                        <td className="bg-info"
                                            style={{width: '72px', borderBottom: 0}}>{!!v.Refreshmemt ? 'Yes' : 'No'}</td>
                                        <td className="bg-info"
                                            style={{width: '72px', borderBottom: 0}}>{!!v['s.GIVE_AWAY'] ? 'Yes' : 'No'}</td>
                                        <td className="bg-warning"
                                            style={{width: '72px', borderBottom: 0}}>{!!v.CALL_ID ? 'Yes' : 'No'}</td>
                                        <td className="bg-warning"
                                            style={{width: '92px',borderRight: 0, borderBottom: 0}}>{v.CALL_STATUS_NAME}</td>
                                        <td className="bg-success"
                                            style={{width: '120px',borderRight: 0, borderBottom: 0}}>{v.MC_CALL_STATUS_NAME}</td>
                                        <th style={{width: '92px', borderBottom: 0}}>
                                            <button className="btn btn-sm btn-primary btn-block"
                                                    onClick={function () {$this.gotoCallForm(v.SMS_ID)}}>Call
                                            </button>
                                        </th>
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
    gotoCallForm: function (SMS_ID) {
        site.hash.goto('/call', {'sms_id': SMS_ID});
    },
    tableHeight: function (height) {
        return height - 60;
    },
    bodyHeight: function () {
        return $(window).height() - 52;
    },
    updateData: function (data) {
        var $this = this;
        this.setState(this.interceptState({data: data, __render: !$this.state.__render}));
    },
    interceptState: function (state) {
        state.data.forEach(function (v, i) {
            v.index = i;
        });
        return state;
    }
});