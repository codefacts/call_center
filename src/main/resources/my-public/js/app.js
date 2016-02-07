site.reactjs.App = React.createClass({
    getInitialState: function () {
        return {
            body: "",
            bodyRef: null,
            user: {},
            campaign: {}
        };
    },

    componentDidMount: function () {
        var $this = this;
        $this.router();
        $this.getUserData();
        $this.getCampaign();
    },

    render: function () {
        var $this = this;
        var user = $this.state.user;
        var campaign = $this.state.campaign;
        return (
            <div className="container-fluid">

                <div className="row">
                    <div className="col-md-12">
                        <site.reactjs.NavbarPrimary user={user} campaign={campaign}/>
                    </div>
                </div>

                {$this.state.body}

            </div>
        );
    },
    router: function () {
        var $this = this;
        site.hash
            .on('/', function () {
                $this.setState({
                    body: <site.reactjs.Step1 onInit={$this.onInitBody}/>
                }, function () {
                    $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .on('/work-day-details', function () {
                $this.setState({
                    body: <site.reactjs.Step2 onInit={$this.onInitBody}/>
                }, function () {
                    $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .on('/call', function () {
                $this.setState({
                    body: <site.reactjs.ContactDetailsAndCall onInit={$this.onInitBody}/>
                }, function () {
                    $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .on('/grocery-form', function () {
                $this.setState({
                    body: <site.reactjs.grocery.Step1 onInit={$this.onInitBody}/>
                }, function () {
                    console.log("updating contacts: " + $this.bodyRef.updateData);
                    if ($.isFunction($this.bodyRef.updateData)) $this.bodyRef.updateData(site.hash.getParams());
                });
            })
            .execute()
        ;
    },
    onInitBody: function (comp) {
        this.bodyRef = comp;
    },
    getUserData: function () {
        var $this = this;
        $.ajax({
            url: '/current-user',
            cache: false,
            success: function (user) {
                console.log(user)
                $this.setState({
                    user: user
                });
            },
            error: function () {
            }
        });
    },
    getCampaign: function () {
        var $this = this;
        $.ajax({
            url: '/current-campaign',
            cache: false,
            success: function (campaign) {

                $this.setState({
                    campaign: campaign
                });
            },
            error: function () {
            }
        });
    }
});