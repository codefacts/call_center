site.reactjs.NavbarPrimary = React.createClass({
    getInitialState: function () {
        return {
            user: {}
        };
    },
    componentDidMount: function () {
        this.getUser();
    },
    render: function () {
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

                        <a className="navbar-brand" href="#">{this.state.username}</a>
                    </div>

                    <div className="collapse navbar-collapse" id="bs-example-navbar-collapse-1">

                        <ul className="nav navbar-nav">
                            <li className="">
                                <a href="#/">
                                    Dashboard
                                </a>
                            </li>
                            <li>
                                <a href="#/contacts">
                                    Contacts
                                </a>
                            </li>
                            <li>
                                <a href="#/summary">
                                    Summary
                                </a>
                            </li>
                        </ul>

                        <ul className="nav navbar-nav navbar-right">
                            <li>
                                <a type="button" className="btn btn-lg navbar-btn btn-block"
                                   href="/logout"
                                   style={{padding: '12px', margin: 0}}>
                                    <span className="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
        );
    },
    getUser: function () {
        var $this = this;
        $.ajax({
            url: '/currentUser',
            cache: false,
            success: function (user) {
                $this.setState({
                    user: user
                });
            },
        });
    }
});