site.reactjs.MorrisBar = React.createClass({
    getDefaultProps: function () {
        return {
            id: "",
            style: {height: '450px'},
            data: [],
            onInit: function () {
            }
        }
    },
    getInitialState: function () {
        var $this = this;
        return {};
    },
    componentDidMount: function () {
        var $this = this;
        $this.props.onInit($this);

        Morris.Bar(merge2($this.props.barConfig, {element: $this.props.id}));

    },
    render: function () {
        var $this = this;

        return (

            <div id={$this.props.id} style={$this.props.style}></div>
        );
    }
});