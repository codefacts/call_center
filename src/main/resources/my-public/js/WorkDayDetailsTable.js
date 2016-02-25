site.reactjs.WorkDayDetailsTable = React.createClass({
    getDefaultProps: function () {
        return {
            data: [],
            onInit: function () {
            },
            user: {},
        };
    },
    getInitialState: function () {
        return this.interceptState({
            bodyHeight: this.bodyHeight(),
            data: this.props.data,
            __render: true,
        });
    },
    componentDidMount: function () {
        var $this = this;
        $this.props.onInit($this);
        $(window).bind("resize", $this.onWindowResize);

        $this.registerEventBusHandler();

    },

    componentWillUnmount: function () {
        var $this = this;
        $(window).unbind("resize", $this.onWindowResize);

        if (eb.state === EventBus.OPEN) {
            $this.unregisterEventBusHandler();
        }
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
                        <th className="bg-primary" style={{borderBottom: 0}}>SMS ID</th>
                        <th className="bg-primary" style={{borderBottom: 0}}>Consumer</th>
                        <th className="bg-primary" style={{borderBottom: 0}}>Mobile</th>
                        <th className="bg-success" style={{borderBottom: 0}}>Age</th>
                        <th className="bg-danger" style={{borderBottom: 0}}>PTR</th>

                        <th className="bg-danger" style={{borderBottom: 0}}>SWP</th>
                        <th className="bg-info" style={{borderBottom: 0}}>REF.</th>
                        <th className="bg-info" style={{borderBottom: 0}}>G.A.</th>

                        <th className="bg-warning" style={{borderBottom: 0}}>Call Status</th>
                        <th className="bg-warning" style={{borderBottom: 0}}>M.C. Status</th>
                        <th className="bg-warning" style={{borderBottom: 0}}></th>
                    </tr>
                    </thead>
                    <tbody className="MainTableBody">
                    {(function () {

                        return (
                            data.map(function (v) {

                                var call_op_locked_by = (site.CALL_OPERATORS[v.LOCKED_BY] || {});

                                return (
                                    <tr key={v.index}>
                                        <td className="bg-primary"
                                            style={{borderBottom: 0}}>{v.SMS_ID}</td>
                                        <td className="bg-primary"
                                            style={{borderBottom: 0}}>{v.Customer_Name}</td>
                                        <td className="bg-primary"
                                            style={{borderBottom: 0}}>{v.CONSUMER_MOBILE}</td>
                                        <td className="bg-success"
                                            style={{borderBottom: 0}}>{v['s.AGE']}</td>
                                        <td className="bg-danger"
                                            style={{borderBottom: 0}}>{!!v['s.PTR'] ? 'Yes' : 'No'}</td>

                                        <td className="bg-danger"
                                            style={{borderBottom: 0}}>{!!v['SWAP'] ? 'Yes' : 'No'}</td>
                                        <td className="bg-info"
                                            style={{borderBottom: 0}}>{!!v.Refreshmemt ? 'Yes' : 'No'}</td>
                                        <td className="bg-info"
                                            style={{borderBottom: 0}}>{!!v['s.GIVE_AWAY'] ? 'Yes' : 'No'}</td>

                                        <td className="bg-warning"
                                            style={{borderRight: 0, borderBottom: 0}}>{v.CALL_STATUS_NAME}</td>
                                        <td className="bg-success"
                                            style={{borderRight: 0, borderBottom: 0}}>{v.MC_CALL_STATUS_NAME}</td>
                                        <th style={{borderBottom: 0}}>
                                            <button className="btn btn-sm btn-primary btn-block"
                                                    disabled={!!v.LOCKED_BY}
                                                    title={call_op_locked_by.CALL_OPERATOR_NAME + ' [#' + call_op_locked_by.CALL_OPERATOR_ID + ']'}
                                                    onClick={function () {$this.gotoCallForm(v.SMS_ID)}}>
                                                {!!v.LOCKED_BY ? 'Calling' : 'Call'}
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

    registerEventBusHandler: function () {
        var $this = this;
        eb.registerHandler('LOCK_CONTACT_ID', null, $this.lock);
        eb.registerHandler('UN_LOCK_CONTACT_ID', null, $this.unLock);
        eb.registerHandler("CONTACT_UPDATED", null, $this.updateContact);
        console.log("EB registered")
    },

    unregisterEventBusHandler: function () {
        var $this = this;
        eb.unregisterHandler('LOCK_CONTACT_ID', null, $this.lock);
        eb.unregisterHandler('UN_LOCK_CONTACT_ID', null, $this.unLock);
        eb.unregisterHandler("CONTACT_UPDATED", null, $this.updateContact);
        console.log("EB Unregistered")
    },

    updateContact: function (err, msg) {
        var $this = this;
        var contact = msg.body;
        contact.sms_id = parseInt(contact.sms_id);
        var data = $this.state.data;
        var found = data.find(function (rr) {
            return rr.SMS_ID == contact.sms_id;
        });

        if (!!found) {
            var call_status_id = parseInt(contact.call_status);
            found.CALL_OPERATOR = parseInt(contact.agent_id);
            found['c.CALL_STATUS_ID'] = call_status_id;
            found['CALL_STATUS_NAME'] = window.site.CALL_STATUSES.find(function (rr) {
                return rr['CALL_STATUS_ID'] == call_status_id;
            }).CALL_STATUS_NAME;
            found.DATASOURCE = parseInt(contact.DATASOURCE);
        }

        this.setState({
            data: data,
            __render: !$this.state.__render
        });
    },

    lock: function (err, msg) {
        console.log("LOCK EVENT")
        console.log(msg)
        var $this = this;
        var SMS_ID = parseInt(msg.body.SMS_ID);
        var data = this.state.data;
        var v = data.find(function (val) {
                return val.SMS_ID === SMS_ID;
            }) || {};
        if (!!v.LOCKED_BY) {
            return;
        }
        v.LOCKED_BY = msg.body.CALL_OPERATOR;
        this.setState({
            data: data,
            __render: !$this.state.__render
        });
    },

    unLock: function (err, msg) {
        console.log("UNLOCK EVENT")
        console.log(msg)
        var $this = this;
        var SMS_ID = parseInt(msg.body.SMS_ID);
        var data = this.state.data;
        var v = data.find(function (val) {
                return (val.SMS_ID === SMS_ID) && (val.LOCKED_BY === msg.body.CALL_OPERATOR);
            }) || {};
        v.LOCKED_BY = false;
        this.setState({
            data: data,
            __render: !$this.state.__render
        });
    },

    gotoCallForm: function (SMS_ID) {
        var $this = this;

        eb.publish('LOCK_CONTACT_ID', {
            SMS_ID: SMS_ID,
            CALL_OPERATOR: window.currentUser.CALL_OPERATOR_ID
        });

        site.hash.goto('/call', {'sms_id': SMS_ID, call_operator: window.currentUser.CALL_OPERATOR_ID});
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