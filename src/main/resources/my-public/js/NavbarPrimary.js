site.reactjs.NavbarPrimary = React.createClass({
    getDefaultProps: function () {
        return {
            user: {}
        };
    },
    render: function () {
        var user = this.props.user;
        return (
            <nav className="navbar navbar-default" style={{marginBottom: 0}}>
                <div className="container-fluid">

                    <div className="navbar-header">
                        <button type="button" className="navbar-toggle collapsed" data-toggle="collapse"
                                data-target="#bs-example-navbar-collapse-1" aria-expanded="false"><span
                            className="sr-only">Toggle navigation</span>
                            <span className="icon-bar"></span> <span className="icon-bar"></span> <span
                                className="icon-bar"></span>
                        </button>

                        <a className="navbar-brand" href="#">{user.username} [#{user.userId}]</a>
                    </div>

                    <div className="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                        <ul className="nav navbar-nav navbar-right">
                            <li>
                                <a href="/logout" className="btn btn-lg navbar-btn btn-block"
                                   style={{padding: '12px', margin: 0}}>
                                    <span className="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
        );
    }
});