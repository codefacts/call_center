site.reactjs.CallSubmitForm = React.createClass({
    getDefaultProps: function () {
        return {
            data: {},
            callOperator: {},
            brands: [],
        };
    },
    getInitialState: function () {
        var $this = this;
        return {
            CALL_ID: $this.props.data['CALL_ID'],
            form: {}
        };
    },
    componentDidMount: function () {
        var $this = this;
        console.log($this.props.data['CALL_ID'] + " call id")
        this.setState({CALL_ID: $this.props.data['CALL_ID']});
    },
    render: function () {
        var $this = this;
        var data = this.props.data;
        var callOperator = this.props.callOperator;
        var brands = this.props.brands;
        var form = this.state.form;
        var CALL_ID = this.state['CALL_ID'];

        return (
            <div className="panel panel-default">
                <div className="panel-heading">
                    <div className="row">
                        <div className="col-md-6">
                            <h3 className="panel-title">{'Question & Answer'}</h3>
                        </div>
                        <div className="col-md-6">
                            <strong>{'CALL ID: ' + (!!CALL_ID ? CALL_ID : (!!$this.props.data.CALL_ID ? $this.props.data.CALL_ID : ''))}</strong>
                        </div>
                    </div>
                </div>
                <div className="panel-body">
                    <div className="well well-sm">
                        <div className="row ">
                            <div className="col-md-6">Call Agent: <strong>{callOperator.CALL_OPERATOR_NAME}</strong>
                            </div>
                            <div className="col-md-6">Call Date: <strong>{formatDate(new Date())}</strong></div>
                        </div>
                    </div>

                    <form id="call-submit-form" className="form-horizontal" onSubmit={$this.submitData}>

                        <input type="hidden" name="CONSUMER_MOBILE" value={data['CONSUMER_MOBILE']}/>
                        <input type="hidden" name="Customer_Name" value={data['Customer_Name']}/>

                        <input type="hidden" name="br_id" value={data['s.BR_ID']}/>
                        <input type="hidden" name="sms_id" value={data.SMS_ID}/>
                        <input type="hidden" name="agent_id" value={callOperator.CALL_OPERATOR_ID}/>
                        <input type="hidden" name="house_id" value={data['h.DISTRIBUTION_HOUSE_ID']}/>

                        <site.reactjs.CallSubmitFormBNH data={data} form={form} brands={brands}
                                                        onValueChange={$this.onValueChange}/>

                        <div className="well well-sm">
                            <div className="row contact-details-row">
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               name="ptr"
                                               checked={form.ptr}
                                               onChange={$this.onValueChange}/>
                                        <span className="call_checkbox_label">PTR</span>
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div className="well well-sm">
                            <div className="row contact-details-row">
                                <div className="col-md-3">
                                    <strong className="call-form-label">Call Status</strong>
                                </div>
                                <div className="col-md-9">
                                    <select className="form-control call-control"
                                            name="call_status" value={form.call_status}
                                            onChange={$this.onValueChange}>
                                        <option value="">Select Status</option>
                                        <option value="1">Success</option>
                                        <option value="2">Mobile Off</option>
                                        <option value="6">Wrong Number</option>
                                        <option value="7">Invalid Number</option>
                                        <option value="8">Call Later On</option>
                                        <option value="10">Others</option>
                                        <option value="11">Not Interested</option>
                                        <option value="14">BR Not Commucated</option>
                                        <option value="19">Call Received by Others</option>
                                        <option value="20">No Answer</option>
                                    </select>
                                </div>
                            </div>

                            <div className="row contact-details-row">
                                <div className="col-md-3">
                                    <span className="call-form-label">Remarks</span>
                                </div>
                                <div className="col-md-9">
                                    <textarea className="form-control" name="remark" value={form.remark}
                                              onChange={$this.onValueChange}/>
                                </div>

                            </div>


                            <div className="row">
                                <div className="col-md-4"></div>
                                <div className="col-md-4">
                                    <span className="btn btn-danger btn-block"
                                          onClick={$this.clearForm}>Clear</span>
                                </div>
                                <div className="col-md-4">
                                    <input className="btn btn-primary btn-block" type="submit"
                                           value="Submit"/>
                                </div>
                            </div>
                        </div>
                    </form>

                </div>
            </div >
        );
    },
    onValueChange: function (e) {
        if ($(e.target).is(':checkbox')) {
            var state = {};
            state[e.target.name] = !!e.target.checked;
            var form = copy(this.state.form);
            form = merge2(form, state);
            this.setState({form: form});
            return;
        }
        var state = {};
        state[e.target.name] = e.target.value;
        var form = copy(this.state.form);
        form = merge2(form, state);
        this.setState({form: form});
        this.setState(state);
    },
    clearForm: function () {
        var form = copy(this.state.form);
        for (var x in form) {
            var val = form[x];
            if ((typeof  val) == "boolean") {
                form[x] = false;
            } else if ((typeof  val) == "string") {
                form[x] = "";
            } else if ((typeof  val) == "number") {
                form[x] = 0;
            }
        }
        this.setState({
            form: form
        });
    },
    submitData: function (e) {
        var $this = this;
        e.preventDefault();

        if (!$this.state.form.call_status) {
            alert("Please select call status");
            return;
        }

        var condition = (!!$this.state.form.IS_CONSUMER_SAID_ABOUT_TASTE || !!$this.state.form.CONSUMER_SAID_ABOUT_TASTE)
            && !(!!$this.state.form.IS_CONSUMER_SAID_ABOUT_TASTE && !!$this.state.form.CONSUMER_SAID_ABOUT_TASTE);
        if (condition) {
            if (!!$this.state.form.IS_CONSUMER_SAID_ABOUT_TASTE) {
                alert("What did Consumer say about Taste?");
                return;
            } else {
                alert("Did Consumer say about Taste?");
                return;
            }
        }

        $.ajax({
            url: "/call/create",
            cache: false,
            method: "post",
            data: $('#call-submit-form').serialize(),
            success: function (js) {
                if (js.status == 'success') {
                    $this.setState({CALL_ID: js.call_id}, function () {
                        alert("Call submit successfull. Check your call id at the top of the call form.");
                    });
                } else {
                    alert(js.message);
                }
            },
            error: function (e) {
                alert("Failed. There is error in the server. Try again.");
            }
        });
    }
});