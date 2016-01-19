site.reactjs.Filter = React.createClass({
    getDefaultProps: function () {
        return {
            onHeaderClick: function () {
                console.log("GGGG")
            },
            onSubmitButtonClick: function () {
            },
            onClearButtonClick: function () {
            },
            body: ""
        };
    },
    getInitialState: function () {
        var $this = this;
        return {
            styles: {top: ($this.top() + 'px'), height: $this.height() + 'px', width: $this.width() + 'px'},
            bodyHeight: $this.bodyHeight() + 'px',
        };
    },
    componentDidMount: function () {
        var $this = this;
        $(window).bind("resize", $this.onWindowResize);
    },
    componentWillUnmount: function () {
        var $this = this;
        $(window).unbind("resize", $this.onWindowResize);
    },
    render: function () {
        var $this = this;
        return (
            <section id="cartStickyHeader" style={$this.state.styles}>

                <div className="panel panel-default">
                    <div className="panel-heading" onClick={$this.props.onHeaderClick}>
                        <div className="row">
                            <div className="col-md-10">
                                <h3 className="panel-title" style={{marginTop: '5px'}}>Filters</h3>
                            </div>
                            <div className="col-md-2">
                                <button type="button" className="btn btn-sm pull-right">
                                    <span className="glyphicon glyphicon-list" aria-hidden="true"></span>
                                </button>
                            </div>
                        </div>
                    </div>
                    <div className="panel-body" style={{padding: 0, overflow: 'auto', height: $this.state.bodyHeight}}>

                        {$this.props.body}

                    </div>

                    <div className="panel-footer text-right">
                        <p>
                            <button type="button" className="btn btn-danger btn-lg" style={{marginRight: '10px'}}
                                    onClick={$this.props.onClearButtonClick}>
                                Clear
                            </button>
                            <button type="button" className="btn btn-primary btn-lg"
                                    onClick={$this.props.onSubmitButtonClick}>Submit
                            </button>
                        </p>
                    </div>
                </div>

            </section>
        );
    },
    width: function () {
        return 500;
    },
    top: function () {
        return $(window).height() * 0;
    },
    height: function () {
        return $(window).height();
    },
    bodyHeight: function () {
        return $(window).height() - (77 + 38);
    },
    onWindowResize: function () {
        var $this = this;
        $this.setState({
            styles: {top: ($this.top() + 'px'), height: $this.height() + 'px', width: $this.width() + 'px'},
            bodyHeight: $this.bodyHeight() + 'px'
        });
    }
});