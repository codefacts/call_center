site.reactjs.ContactDetailsAndCall = React.createClass({
    getDefaultProps: function () {
        return {
            CONTACT_DETAILS_URL: '/consumer-contacts/details',
            CALL_OPERATOR_URL: '/call-operator',
            BRANDS_URL: '/brands',
            onInit: function () {
            }
        };
    },
    getInitialState: function () {
        return {
            data: {},
            callOperator: {},
            brands: []
        }
    },
    componentDidMount: function () {
        var $this = this;
        var params = site.hash.params();
        this.props.onInit(this);
        this.getContactDetails(params);
        this.getCallOperator();
        this.getBrands();

        eb.publish('LOCK_CONTACT_ID', {
            SMS_ID: parseInt(params.sms_id),
            CALL_OPERATOR: parseInt(params.call_operator)
        });
        console.log("EB.published(LOCK_CONTACT_ID): " + JSON.stringify({
                SMS_ID: parseInt(params.sms_id),
                CALL_OPERATOR: parseInt(params.call_operator)
            }));
    },
    componentWillUnmount: function () {
        var $this = this;
        eb.publish('UN_LOCK_CONTACT_ID', {
            SMS_ID: $this.state.data.SMS_ID,
            CALL_OPERATOR: $this.state.callOperator.CALL_OPERATOR_ID
        });
        console.log("EB.published(UN_LOCK_CONTACT_ID): " + JSON.stringify({
                SMS_ID: $this.state.data.SMS_ID,
                CALL_OPERATOR: $this.state.callOperator.CALL_OPERATOR_ID
            }));
    },
    render: function () {
        var $this = this;
        return (
            <div className="row">
                <div className="col-md-6">
                    <site.reactjs.ContactDetails data={$this.state.data}/>
                </div>
                <div className="col-md-6">
                    <site.reactjs.CallSubmitForm data={$this.state.data}
                                                 callOperator={$this.state.callOperator}
                                                 brands={$this.state.brands}/>
                </div>
            </div>
        );
    },
    getCallOperator: function () {
        var $this = this;
        $.ajax({
            url: $this.props.CALL_OPERATOR_URL,
            cache: false,
            success: function (callOperator) {
                $this.setState({callOperator: callOperator});
            },
            error: function () {
            }
        });
    },
    getBrands: function () {
        var $this = this;
        $.ajax({
            url: $this.props.BRANDS_URL,
            cache: false,
            success: function (brands) {
                $this.setState({brands: brands});
            },
            error: function () {
            }
        });
    },
    getContactDetails: function (params) {
        var $this = this;
        $.ajax({
            url: $this.props.CONTACT_DETAILS_URL,
            data: params,
            cache: false,
            success: function (data) {
                $this.setState({data: data});
            },
            error: function () {
            }
        });
    },
    updateData: function (params) {
        this.getContactDetails(params);
    }
});