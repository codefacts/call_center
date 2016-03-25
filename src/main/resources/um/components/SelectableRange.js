window.SelectableRange = React.createClass({

    getDefaultProps: function () {
        return {
            from: "from",
            to: "to"
        };
    }
    ,

    getInitialState: function () {
        return {
            from: this.props.from,
            to: this.props.to,
            value: (this.props.from + " - " + this.props.to)
        };
    }
    ,

    onFromChange: function (e) {
        this.setState({from: e.target.value});
    }
    ,

    onToChange: function (e) {
        this.setState({to: e.target.value});
    }
    ,

    onClear: function (e) {
        this.setState({from: "", to: ""});
    }
    ,

    okClick: function (e) {
        console.log(this.state.from + " - " + this.state.to)
        this.setState({
            value: (this.state.from + " - " + this.state.to)
        });
    }
    ,

    onClick: function (e) {
        $('.modal').modal('show');
    },

    onChange: function (e) {
        console.log(e.target.value);
        this.setState({
            value: e.target.value
        });
    },

    render: function () {

        var modalBody = (

            <div className="row">

                <div className="col-md-6">
                    <div className="form-group">
                        <label htmlFor="exampleInputEmail1">From</label>
                        <input type="number" className="form-control" id="exampleInputEmail1" placeholder="From"
                               value={this.state.from}
                               onChange={this.onFromChange}/>
                    </div>
                </div>

                <div className="col-md-6">
                    <div className="form-group">
                        <label htmlFor="exampleInputEmail1">To</label>
                        <input type="number" className="form-control" id="exampleInputEmail1" placeholder="To"
                               value={this.state.to}
                               onChange={this.onToChange}/>
                    </div>
                </div>

            </div>

        );

        return (
            <div className="form-group">
                <input id="call_range" type="text" className="form-control range" placeholder="Call No. Between"
                       name="call_range"
                       value={this.state.value}
                       onChange={this.onChange}
                       onClick={this.onClick}/>
                <Modal body={modalBody} onClear={this.onClear} okClick={this.okClick}/>
            </div>
        );
    }
})
;