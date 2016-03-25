window.Range = React.createClass({

    getDefaultProps: function () {
        return {
            id: "",
            from: "",
            to: "",
            name: "",
            title: "",
            placeholder: "",
            modalId: "",
            modalTitle: "",
            onChange: function () {
            }
        };
    },

    getInitialState: function () {
        return {
            isModalVisible: false
        };
    },

    onFromChange: function (e) {
        this.props.onChange({
            from: e.target.value,
            to: this.props.to
        });
    },

    onToChange: function (e) {
        this.props.onChange({
            from: this.props.from,
            to: e.target.value,
        });
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

    onChange: function (e) {
        this.setState({value: e.target.value});
    },

    render: function () {
        var $this = this;
        var modalBody = (

            <div className="row">

                <div className="col-md-6">
                    <div className="form-group">
                        <label htmlFor="exampleInputEmail1">From</label>
                        <input type="number" className="form-control" id="exampleInputEmail1" placeholder="From"
                               value={this.props.from}
                               onChange={this.onFromChange}/>
                    </div>
                </div>

                <div className="col-md-6">
                    <div className="form-group">
                        <label htmlFor="exampleInputEmail1">To</label>
                        <input type="number" className="form-control" id="exampleInputEmail1" placeholder="To"
                               value={this.props.to}
                               onChange={this.onToChange}/>
                    </div>
                </div>

            </div>

        );

        var modalFooter = (<div>
            <span className="btn btn-danger" onClick={$this.onClear}>Clear</span>
            <span className="btn btn-primary" style={{width: '100px'}} onClick={$this.onModalClose}>Ok</span>
        </div>);

        var value = (((this.props.from != "") || (this.props.to != "")) ? (this.props.from + " - " + this.props.to) : "");

        return (
            <div>
                <input id={this.props.id} type="text" className="form-control range"
                       placeholder={this.props.placeholder}
                       title={this.props.title}
                       name={this.props.name}
                       value={value}
                       onChange={this.onChange}
                       onClick={this.onClick}/>

                {!!$this.state.isModalVisible ? (<Modal id={$this.props.modalId} title={$this.props.modalTitle}
                                                        onClose={$this.onModalClose}
                                                        isOpen={$this.state.isModalVisible}
                                                        body={modalBody}
                                                        footer={modalFooter}/>) : ""}
            </div>
        );
    }
})
;