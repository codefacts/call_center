window.Modal = React.createClass({
    getDefaultProps: function () {
        var $this = this;
        return {
            id: Math.random(),
            title: "Title",
            body: "body",
            footer: "footer",
            isOpen: true,
            onClose: function () {},
        };
    },
    getInitialState: function () {
        var $this = this;
        return {
        };
    },
    render: function () {
        var $this = this;
        var style = {};
        if ($this.props.isOpen) {
            style.display = "block";
        }

        var onClose = $this.props.onClose;

        return (

            <div>
                <div className="modal" id="myModal" tabIndex="-1" role="dialog" aria-labelledby="myModalLabel"
                     style={style}>
                    <div id={$this.props.id} className="modal-dialog" role="document">
                        <div className="modal-content">
                            <div className="modal-header">
                                <button type="button" className="close" aria-label="Close"
                                        onClick={onClose}>
                                    <span aria-hidden="true">&times;</span>
                                </button>
                                <h4 className="modal-title" id="myModalLabel">{$this.props.title}</h4>
                            </div>
                            <div className="modal-body">
                                {$this.props.body}
                            </div>
                            <div className="modal-footer">
                                {$this.props.footer}
                            </div>
                        </div>
                    </div>
                </div>
                {!!$this.props.isOpen ? (<div className="modal-backdrop" style={{opacity: 0.5}}></div>) : ""}
            </div>

        );
    }
});