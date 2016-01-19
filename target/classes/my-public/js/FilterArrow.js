site.reactjs.FilterArrow = React.createClass({
    getDefaultProps: function () {
        return {
            onClick: function () {
                console.log("clicked");
            },
            inactiveOpacity: 0.5,
            activeOpacity: 1
        };
    },
    getInitialState: function () {
        var $this = this;
        return {
            top: ($this.top() + 'px'),
            opacity: $this.props.inactiveOpacity
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
            <section id="stickyHeader" style={{top: $this.state.top, opacity: $this.state.opacity}}
                     onClick={$this.props.onClick}
                     onMouseOver={$this.activeOpaque} onMouseLeave={$this.inactiveOpaque}>

                <div className="row">
                    <div className="col-md-12">
                        <button type="button" className="btn btn-default btn-lg btn-block" style={{padding: '3px'}}>
                            <span className="glyphicon glyphicon-list" aria-hidden="true"></span>
                        </button>
                    </div>
                </div>

            </section>
        );
    },
    activeOpaque: function () {
        this.setState({opacity: this.props.activeOpacity});
    },
    inactiveOpaque: function () {
        this.setState({opacity: this.props.inactiveOpacity});
    },
    top: function () {
        return $(window).height() * 0.40;
    },
    onWindowResize: function () {
        var $this = this;
        $this.setState({top: $this.top() + 'px'});
    }
});