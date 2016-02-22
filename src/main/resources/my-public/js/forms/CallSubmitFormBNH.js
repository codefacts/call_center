site.reactjs.CallSubmitFormBNH = React.createClass({
    getDefaultProps: function () {
        return {
            form: {},
            onInit: function () {
            },
            onValueChange: function () {
            },
            brands: [],
        };
    },
    getInitialState: function () {
        return {
            TALKING_ABOUT_WHAT: true,
            Q02_NOTICED_NEW_CHANGE: true,
            Q03_CHANGES_HAS_NOTICED: true,
            Q04_OPINION_ABOUT_MODERN_STICK_DESIGN: true,
            Q05_A_LIKED_NEW_DESIGN: true,
            Q05_B_NOT_LIKED_NEW_DESIGN: true,
            IS_CONSUMER_SAID_ABOUT_TASTE: true,
            CONSUMER_SAID_ABOUT_TASTE: true
        };
    },
    componentWillReceiveProps: function (props) {
        var $this = this;
        var form = props.form;
        var state = {};
        if (!form.Q01_BR_CONTACT) {
            $this.setState({
                TALKING_ABOUT_WHAT: true,
                Q02_NOTICED_NEW_CHANGE: true,
                Q03_CHANGES_HAS_NOTICED: true,
                Q04_OPINION_ABOUT_MODERN_STICK_DESIGN: true,
                Q05_A_LIKED_NEW_DESIGN: true,
                Q05_B_NOT_LIKED_NEW_DESIGN: true,
                IS_CONSUMER_SAID_ABOUT_TASTE: true,
                CONSUMER_SAID_ABOUT_TASTE: true
            });
        } else {

            if ((form.TALKING_ABOUT_WHAT + "") == '1') {
                state.Q04_OPINION_ABOUT_MODERN_STICK_DESIGN = false;
                state.Q02_NOTICED_NEW_CHANGE = true;

                if (!form.Q04_OPINION_ABOUT_MODERN_STICK_DESIGN) {
                    state.Q05_A_LIKED_NEW_DESIGN = true;
                    state.Q05_B_NOT_LIKED_NEW_DESIGN = true;
                } else if (form.Q04_OPINION_ABOUT_MODERN_STICK_DESIGN <= 2) {
                    state.Q05_A_LIKED_NEW_DESIGN = false;
                    state.Q05_B_NOT_LIKED_NEW_DESIGN = true;
                } else {
                    state.Q05_A_LIKED_NEW_DESIGN = true;
                    state.Q05_B_NOT_LIKED_NEW_DESIGN = false;
                }

            } else if (form.TALKING_ABOUT_WHAT == '2') {

                state.Q04_OPINION_ABOUT_MODERN_STICK_DESIGN = true;
                state.Q02_NOTICED_NEW_CHANGE = false;

                state.Q04_OPINION_ABOUT_MODERN_STICK_DESIGN = !form.Q02_NOTICED_NEW_CHANGE;

                if (!!form.Q02_NOTICED_NEW_CHANGE) {
                    if (!form.Q04_OPINION_ABOUT_MODERN_STICK_DESIGN) {
                        state.Q05_A_LIKED_NEW_DESIGN = true;
                        state.Q05_B_NOT_LIKED_NEW_DESIGN = true;
                    } else if (form.Q04_OPINION_ABOUT_MODERN_STICK_DESIGN <= 2) {
                        state.Q05_A_LIKED_NEW_DESIGN = false;
                        state.Q05_B_NOT_LIKED_NEW_DESIGN = true;
                    } else {
                        state.Q05_A_LIKED_NEW_DESIGN = true;
                        state.Q05_B_NOT_LIKED_NEW_DESIGN = false;
                    }
                } else {
                    state.Q05_A_LIKED_NEW_DESIGN = true;
                    state.Q05_B_NOT_LIKED_NEW_DESIGN = true;
                }

            } else {
                state.Q04_OPINION_ABOUT_MODERN_STICK_DESIGN = true;
                state.Q02_NOTICED_NEW_CHANGE = true;
                state.Q05_A_LIKED_NEW_DESIGN = true;
                state.Q05_B_NOT_LIKED_NEW_DESIGN = true;
            }

            if (!form.IS_CONSUMER_SAID_ABOUT_TASTE) {
                state.CONSUMER_SAID_ABOUT_TASTE = true;
            }
            $this.setState(state);
        }
    },
    componentDidMount: function () {
        this.props.onInit(this);
    },
    clearForm: function () {

    },
    render: function () {
        var $this = this;
        var brands = this.props.brands;
        var form = this.props.form;
        var data = this.props.data;
        var onValueChange = this.onValueChange;

        return (
            <div className="well well-sm">
                <div className="row contact-details-row">
                    <div className="col-md-12">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="Q01_BR_CONTACT"
                                   value={1}
                                   checked={form.Q01_BR_CONTACT}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Q1. BR Contacted? </span>
                        </label>
                    </div>

                    <div className="col-md-12" style={{marginTop: '13px'}}>
                        <div className="row">
                            <div className="col-md-8">
                                <strong className="call-form-label">Q2. Br communicated about</strong>
                            </div>
                            <div className="col-md-4">
                                <select className="form-control call-control"
                                        name="TALKING_ABOUT_WHAT"
                                        value={form.TALKING_ABOUT_WHAT}
                                        disabled={$this.state.TALKING_ABOUT_WHAT}
                                        onChange={$this.onValueChange}>
                                    <option value="">Select Status</option>
                                    <option value="1">Stick Design</option>
                                    <option value="2">Can't remember</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div className="col-md-12">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="Q02_NOTICED_NEW_CHANGE"
                                   value={1}
                                   checked={form.Q02_NOTICED_NEW_CHANGE}
                                   disabled={$this.state.Q02_NOTICED_NEW_CHANGE}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Q3. Noticed New Change?</span>
                        </label>
                    </div>

                </div>

                <div className="row contact-details-row">
                    <div className="col-md-8">
                        <strong className="call-form-label">Q4. Opinion on Stick Design</strong>
                    </div>
                    <div className="col-md-4">
                        <select className="form-control call-control"
                                name="Q04_OPINION_ABOUT_MODERN_STICK_DESIGN"
                                value={form.Q04_OPINION_ABOUT_MODERN_STICK_DESIGN}
                                disabled={$this.state.Q04_OPINION_ABOUT_MODERN_STICK_DESIGN}
                                onChange={$this.onValueChange}>
                            <option value="">Select Status</option>
                            <option value="1">Good</option>
                            <option value="2">Ok/Average</option>
                            <option value="3">Bad</option>
                        </select>
                    </div>
                </div>

                <div className="row contact-details-row">
                    <div className="col-md-8">
                        <strong className="call-form-label">Q5(A). Positive Feedback</strong>
                    </div>
                    <div className="col-md-4">
                        <select className="form-control call-control"
                                name="Q05_A_LIKED_NEW_DESIGN"
                                value={form.Q05_A_LIKED_NEW_DESIGN}
                                disabled={$this.state.Q05_A_LIKED_NEW_DESIGN}
                                onChange={$this.onValueChange}>
                            <option value="">Select Status</option>
                            <option value="1">Golden Ring</option>
                            <option value="2">{'B&H Written Liner'}</option>
                            <option value="3">{'Both Golden Ring & Written Liner'}</option>
                            <option value="4">Overall Design</option>
                            <option value="5">Other</option>
                        </select>
                    </div>
                </div>

                <div className="row contact-details-row">
                    <div className="col-md-8">
                        <strong className="call-form-label">Q5(B). Negative Feedback</strong>
                    </div>
                    <div className="col-md-4">
                        <select className="form-control call-control"
                                name="Q05_B_NOT_LIKED_NEW_DESIGN"
                                value={form.Q05_B_NOT_LIKED_NEW_DESIGN}
                                disabled={$this.state.Q05_B_NOT_LIKED_NEW_DESIGN}
                                onChange={$this.onValueChange}>
                            <option value="">Select Status</option>
                            <option value="1">Golden Ring</option>
                            <option value="2">{'B&H Written Liner'}</option>
                            <option value="3">{'Both Golden Ring & Written Liner'}</option>
                            <option value="4">Overall Design</option>
                            <option value="5">Other</option>
                        </select>
                    </div>
                </div>

                <div className="row contact-details-row">

                    <div className="col-md-12">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="IS_CONSUMER_SAID_ABOUT_TASTE"
                                   value={1}
                                   checked={form.IS_CONSUMER_SAID_ABOUT_TASTE}
                                   disabled={$this.state.IS_CONSUMER_SAID_ABOUT_TASTE}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Did Consumer say about Taste?</span>
                        </label>
                    </div>

                </div>

                <div className="row contact-details-row">
                    <div className="col-md-8">
                        <strong className="call-form-label">What did Consumer say about Taste?</strong>
                    </div>
                    <div className="col-md-4">
                        <select className="form-control call-control"
                                name="CONSUMER_SAID_ABOUT_TASTE"
                                value={form.CONSUMER_SAID_ABOUT_TASTE}
                                disabled={$this.state.CONSUMER_SAID_ABOUT_TASTE}
                                onChange={$this.onValueChange}>
                            <option value="">Select Status</option>
                            <option value="1">Light</option>
                            <option value="2">Strong</option>
                            <option value="3">Taste Change</option>
                            <option value="4">Negative Feedback</option>
                        </select>
                    </div>
                </div>

            </div>
        )
    },
    onValueChange: function (e) {
        var $this = this;
        $this.props.onValueChange(e);

        switch (e.target.name) {
            case 'Q01_BR_CONTACT':
                $this.setState({
                    TALKING_ABOUT_WHAT: !e.target.checked,
                    IS_CONSUMER_SAID_ABOUT_TASTE: !e.target.checked,
                });
                break;
            case 'TALKING_ABOUT_WHAT':
                if ((e.target.value + "") == '1') {
                    $this.setState({
                        Q04_OPINION_ABOUT_MODERN_STICK_DESIGN: false,
                        Q02_NOTICED_NEW_CHANGE: true,
                    });
                } else if (e.target.value == '2') {
                    $this.setState({
                        Q04_OPINION_ABOUT_MODERN_STICK_DESIGN: true,
                        Q02_NOTICED_NEW_CHANGE: false,
                    });
                } else {
                    $this.setState({
                        Q04_OPINION_ABOUT_MODERN_STICK_DESIGN: true,
                        Q02_NOTICED_NEW_CHANGE: true,
                    });
                }
                break;
            case 'Q02_NOTICED_NEW_CHANGE':
                $this.setState({
                    Q04_OPINION_ABOUT_MODERN_STICK_DESIGN: !e.target.checked,
                });
                break;

            case 'Q04_OPINION_ABOUT_MODERN_STICK_DESIGN':
                if ((e.target.value == '1') || (e.target.value == '2')) {
                    $this.setState({
                        Q05_A_LIKED_NEW_DESIGN: !e.target.value,
                        Q05_B_NOT_LIKED_NEW_DESIGN: !!e.target.value,
                    });
                } else if (e.target.value == '3') {
                    $this.setState({
                        Q05_A_LIKED_NEW_DESIGN: !!e.target.value,
                        Q05_B_NOT_LIKED_NEW_DESIGN: !e.target.value,
                    });
                } else {
                    $this.setState({
                        Q05_A_LIKED_NEW_DESIGN: true,
                        Q05_B_NOT_LIKED_NEW_DESIGN: true,
                    });
                }
                break;
            case 'Q05_A_LIKED_NEW_DESIGN':
                break;
            case 'Q05_B_NOT_LIKED_NEW_DESIGN':
                break;

            case 'IS_CONSUMER_SAID_ABOUT_TASTE':
                $this.setState({
                    CONSUMER_SAID_ABOUT_TASTE: !e.target.checked
                });
                break;
            case 'CONSUMER_SAID_ABOUT_TASTE':
                break;
        }
    }
});