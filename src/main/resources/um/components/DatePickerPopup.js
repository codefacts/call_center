window.DatePickerPopup = React.createClass({

    getDefaultProps: function () {
        return {
            name: "",
            value: "",
            placeholder: "",
            onChange: function () {
            },
            minDate: null,
            maxDate: null,
            formatDate: !!window.formatDate ? window.formatDate : null,
            modalTitle: "Select Date",
            isModalVisible: false
        };
    },

    getInitialState: function () {
        return {
            isModalVisible: !!this.props.isModalVisible
        };
    },

    componsentDidMount: function () {
        var $this = this;
    },

    render: function () {
        var $this = this;

        var modalBody = (
            <DatePicker date={$this.props.value} minDate={$this.props.minDate} maxDate={$this.props.maxDate}
                        onChange={$this.props.onChange}/>);
        var modalFooter = (<div>
            <span className="btn btn-danger" onClick={$this.onClear}>Clear</span>
            <span className="btn btn-default" onClick={$this.onModalClose}>Cancel</span>
            <span className="btn btn-primary" style={{width: '100px'}} onClick={$this.onModalClose}>Ok</span>
        </div>);

        return (
            <div >
                <input className="form-control" type="text" name={$this.props.name}
                       value={$this.formatDate($this.props.value)} onChange={$this.props.onChange}
                       placeholder={$this.props.placeholder} onClick={$this.showModal}/>

                {!!$this.state.isModalVisible ? (
                    <Modal title={$this.props.modalTitle} body={modalBody} footer={modalFooter}
                           onClose={$this.onModalClose}
                           isOpen={!!$this.state.isModalVisible}/>) : ""}
            </div>
        );
    },
    onClear: function () {
        this.props.onChange("");
    },
    onModalClose: function () {
        this.setState({
            isModalVisible: false
        });
    },
    showModal: function () {
        this.setState({isModalVisible: true});
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