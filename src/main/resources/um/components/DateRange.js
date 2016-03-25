window.DateRange = React.createClass({

    getDefaultProps: function () {
        return {
            id: "",
            placeholder: "",
            title: "",
            from: "",
            to: "",
            onChange: function () {
            },
            modalId: "",
            modalTitle: "",
            name: "",
            minDate: null,
            maxDate: null,
            formatDate: !!window.formatDate ? window.formatDate : null
        };
    },

    getInitialState: function () {
        return {
            isModalVisible: false
        };
    },

    componentDidMount: function () {
    },

    onFromChange: function (from) {
        var $this = this;
        this.props.onChange({from: from, to: $this.props.to});
    },

    onToChange: function (to) {
        var $this = this;
        console.log(to)
        this.props.onChange({from: $this.props.from, to: to});
    },

    onClear: function (e) {
        this.props.onChange({from: "", to: ""});
    },

    onClick: function (e) {
        this.setState({isModalVisible: true});
    },

    onModalClose: function () {
        this.setState({isModalVisible: false});
    },

    render: function () {
        var $this = this;

        var modalBody = (

            <div className="row">

                <div className="col-md-6">
                    <div className="form-group">
                        <label htmlFor="exampleInputEmail1">From</label>
                        <DatePicker
                            minDate={$this.props.minDate}
                            date={this.props.from}
                            onChange={this.onFromChange}
                            />
                    </div>
                </div>

                <div className="col-md-6">
                    <div className="form-group">
                        <label htmlFor="exampleInputEmail1">To</label>
                        <DatePicker
                            maxDate={$this.props.maxDate}
                            date={this.props.to}
                            onChange={this.onToChange}/>
                    </div>
                </div>

            </div>

        );

        var modalFooter = (<div>
            <span className="btn btn-danger" onClick={$this.onClear}>Clear</span>
            <span className="btn btn-primary" style={{width: '100px'}} onClick={$this.onModalClose}>Ok</span>
        </div>);

        var value = ((!!this.props.from || !!this.props.to) ? (this.formatDate(this.props.from) + " : " + this.formatDate(this.props.to)) : "");

        return (

            <div>
                <input id={this.props.id} type="text" className="form-control range"
                       placeholder={this.props.placeholder}
                       title={this.props.title}
                       name={this.props.name}
                       value={value}
                       onChange={function (e) {}}
                       onClick={this.onClick}/>
                {!!$this.state.isModalVisible ? (<Modal id={$this.props.modalId} title={$this.props.modalTitle}
                                                        onClose={$this.onModalClose}
                                                        isOpen={$this.state.isModalVisible} body={modalBody}
                                                        footer={modalFooter}/>) : ""}
            </div>

        );
    },
    formatDate: function (date) {
        var $this = this;
        console.log(date)
        if (!date) return "";
        if (!!$this.props.formatDate && !!date) return $this.props.formatDate(new Date(date));
        else return moment(date, "YYYY-MM-DD").format("DD-MMM-YYYY");
    }
})
;