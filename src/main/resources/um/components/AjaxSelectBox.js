window.AjaxSelectBox = React.createClass({
    getDefaultProps: function () {
        return {
            page: 1,
            total: 0,
            name: "",
            value: "",
            size: 100,
            navLength: 5,
            modalId: "",
            modalTitle: "",
            url: "",
            placeholder: "",
            keyValueParser: function (obj) {
                return {key: "", value: ""};
            }
        };
    },
    getInitialState: function () {
        return {
            value: this.props.value,
            data: [],
            pagination: {page: this.props.page, size: this.props.size, total: this.props.total}
        };
    },

    componentDidMount: function () {
        this.requestData(this.state.value);
    },

    onComplete: function (data) {
        $('#' + this.props.modalId).modal('hide');
        $("body").trigger("value-selected", data);
    },

    onInputClick: function (e) {
        $('#' + this.props.modalId).modal('show');
    },

    handleClick: function (e) {
        this.setState({value: $(e.target).attr("data-obj_key")});
        this.onComplete($(e.target).attr("data-obj_key"));
    },

    handleSubmit: function (e) {
        e.preventDefault();
        this.onComplete($("#ajax-145415").val());
    },

    handleKeyEvent: function (e) {
        console.log("aaaa : " + e.target);

        setTimeout(function () {

            this.requestData(e.target.value);

        }.bind(this), 100);

    },

    clear: function () {
        console.log("clearing....");
        this.setState({value: ""}, function () {
            this.requestData("");
        }.bind(this));
    },

    handleValueChange: function (e) {
        this.setState({value: e.target.value});
    },

    handleOkClick: function (e) {
        this.onComplete($("#ajax-145415").val());
    },

    onPageRequest: function (page, size) {
        this.requestData(this.state.value, page, size);
    },

    render: function () {
        var _self = this;

        var rows = this.state.data.map(function (d) {
            d = _self.props.keyValueParser(d);
            return (
                <tr key={Math.random()} obj_key={'row-' + d.key} onClick={_self.handleClick}>
                    <td data-obj_key={d.key}>{d.value}</td>
                </tr>
            );
        });

        var modalBody = (
            <div id="ajax-select-box" className="ajax-select-box">

                <div id="search-input" className="form-group">
                    <input id="ajax-145415" type="text" className="form-control"
                           placeholder="Search" onKeyUp={this.handleKeyEvent}
                           onChange={this.handleValueChange}
                           value={this.state.value}/>
                </div>

                <div className="table-responsive" style={{maxHeight: '350px'}}>
                    <table className="table table-bordered table-striped table-hover">

                        <colgroup>
                            <col className="col-xs-8"/>
                        </colgroup>

                        <tbody>

                        {rows}

                        </tbody>
                    </table>
                </div>


                <Pagination page={this.state.pagination.page}
                            size={this.state.pagination.size}
                            total={this.state.pagination.total}
                            navLength={this.props.navLength}
                            size={this.props.size}
                            onPageRequest={this.onPageRequest}/>

            </div>
        );

        return (
            <div className="form-group">
                <input id="ajax-select" className="form-control ajax-select" onClick={this.onInputClick}
                       placeholder={this.props.placeholder}
                       defaultValue={this.props.defaultValue}
                       name={this.props.name}
                       value={this.state.value}></input>
                <Modal id={this.props.modalId} title={this.props.modalTitle}
                       body={modalBody} onClear={this.clear} okClick={this.handleOkClick}/>
            </div>
        );
    },

    requestData: function (name, page, size) {
        ajax({
            url: this.props.url,
            data: {name: name, page: page || 1, size: size || this.props.size},
            dataType: 'json',
            cache: false,
            success: function (json) {

                this.setState({data: json.data, pagination: json.pagination});

            }.bind(this),
            error: function (xhr, status, err) {
                alert(err.toString());
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    }
});