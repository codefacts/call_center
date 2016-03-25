site.reactjs.FilterFields = React.createClass({
    getDefaultProps: function () {
        return {
            areaUrl: '/areas',
            distributionHouseUrl: '/distribution-houses',
            brUrl: '/brs',
            formId: "",
            onFilterFiledsInit: function () {
            },
            scope: null,
        }
    },
    getInitialState: function () {
        var defaultState = {
            areas: [],
            distributionHouses: [],
            brs: [],
            areaId: "",
            distributionHouseId: "",
            brId: "",
            recallMode: "",
            workDate: {from: "", to: ""},
            success: {from: "", to: ""},
            ptr: {from: "", to: ""},
            swp: {from: "", to: ""},
            refreshment: {from: "", to: ""},
            giveAway: {from: "", to: ""},
            showTools: {from: "", to: ""},
            showVideo: {from: "", to: ""},
            packsell: {from: "", to: ""},
        };
        if (!!this.props.scope && !!this.props.scope.get()) {
            return this.props.scope.get();
        }
        else {
            return defaultState;
        }
    },
    componentDidMount: function () {
        var $this = this;
        $this.props.onFilterFiledsInit($this);
        $this.updateAreas();
        if (!!this.props.scope && !this.props.scope.get()) $this.updateFields(site.hash.params());
    },
    componentWillUnmount: function () {
        this.props.scope.set({});
        console.log("Unmounted")
        if (!!this.props.scope) {
            for (var x in this.state) {
                this.props.scope.get()[x] = this.state[x];
            }
        }
    },
    render: function () {
        var $this = this;
        return (
            <div className="FormFields">
                <form id={$this.props.formId}>
                    <div className="row">

                        <div className="col-md-12">
                            <div className="form-group">
                                <select className="form-control" value={$this.state.areaId}
                                        name="areaId"
                                        onChange={$this.onAreaSelect}>
                                    <option value="">Select Area</option>
                                    {
                                        $this.state.areas.map(function (area) {
                                            return (
                                                <option value={area.id} key={Math.random()}>{area.name}
                                                </option>
                                            );
                                        })
                                    }
                                </select>
                            </div>
                        </div>

                        <div className="col-md-12">
                            <div className="form-group">
                                <select className="form-control" value={$this.state.distributionHouseId}
                                        name="distributionHouseId"
                                        onChange={$this.onDistributionHouseSelect}>
                                    <option value="">Select Distribution House</option>
                                    {
                                        $this.state.distributionHouses.map(function (area) {
                                            return (
                                                <option value={area.id} key={Math.random()}>{area.name}</option>
                                            );
                                        })
                                    }
                                </select>
                            </div>
                        </div>
                        <div className="col-md-12">
                            <div className="form-group">
                                <select className="form-control" name="brId" value={$this.state.brId}
                                        onChange={$this.onBrSelect}>
                                    <option value="">Select BR</option>
                                    {
                                        $this.state.brs.map(function (area) {
                                            return (
                                                <option value={area.id} key={Math.random()}>{area.name}
                                                    [{area.id}]</option>
                                            );
                                        })
                                    }
                                </select>
                            </div>
                        </div>

                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Date Range</label>
                                <DateRange modalId={Math.random()} modalTitle="Select Date Range"
                                           name="work-date-range"
                                           from={$this.state.workDate.from}
                                           to={$this.state.workDate.to} onChange={$this.onWorkDateChange}/>
                            </div>
                        </div>

                        <div className="col-md-4">
                            <div className="form-group">
                                <label htmlFor="recallMode">Show only</label>
                                <select className="form-control" name="recallMode" value={$this.state.recallMode}
                                        onChange={$this.onRecallModeChange}>
                                    <option value="">Called/Not Called</option>
                                    <option value="1">Called</option>
                                    <option value="0">Not Called</option>
                                </select>
                            </div>
                        </div>

                        <div className="col-md-4">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Success Range</label>
                                <Range name="success-range" from={$this.state.success.from} to={$this.state.success.to}
                                       onChange={$this.onSuccessChange}/>
                            </div>
                        </div>

                        <div className="col-md-4">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">PTR Range</label>
                                <Range name="ptr-range" from={$this.state.ptr.from} to={$this.state.ptr.to}
                                       onChange={$this.onPtrChange}/>
                            </div>
                        </div>
                        <div className="col-md-4">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">SWP Range</label>
                                <Range name="swp-range" from={$this.state.swp.from} to={$this.state.swp.to}
                                       onChange={$this.onSwpChange}/>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Refreshment Range</label>
                                <Range name="refreshment-range" from={$this.state.refreshment.from}
                                       to={$this.state.refreshment.to}
                                       onChange={$this.onRefreshmentChange}/>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Give Away Range</label>
                                <Range name="give-away-range" from={$this.state.giveAway.from}
                                       to={$this.state.giveAway.to}
                                       onChange={$this.onGiveAwayChange}/>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Pack Sell Range</label>
                                <Range name="pack-sell-range" from={$this.state.packsell.from}
                                       to={$this.state.packsell.to}
                                       onChange={$this.onPacksellChange}/>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Show Tools Range</label>
                                <Range name="show-tools-range" from={$this.state.showTools.from}
                                       to={$this.state.showTools.to}
                                       onChange={$this.onShowToolsChange}/>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Show Video Range</label>
                                <Range name="show-video-range" from={$this.state.showVideo.from}
                                       to={$this.state.showVideo.to}
                                       onChange={$this.onShowVideoChange}/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        );
    },
    onSuccessChange: function (pair) {
        this.setState({success: {from: pair.from, to: pair.to}});
        console.log(pair);
    },
    onPtrChange: function (pair) {
        this.setState({ptr: {from: pair.from, to: pair.to}});
        console.log(pair);
    },
    onSwpChange: function (pair) {
        this.setState({swp: {from: pair.from, to: pair.to}});
        console.log(pair);
    },
    onPacksellChange: function (pair) {
        this.setState({packsell: {from: pair.from, to: pair.to}});
        console.log(pair);
    },
    onRefreshmentChange: function (pair) {
        this.setState({refreshment: {from: pair.from, to: pair.to}});
        console.log(pair);
    },
    onGiveAwayChange: function (pair) {
        this.setState({giveAway: {from: pair.from, to: pair.to}});
        console.log(pair);
    },
    onShowToolsChange: function (pair) {
        this.setState({showTools: {from: pair.from, to: pair.to}});
        console.log(pair);
    },
    onShowVideoChange: function (pair) {
        this.setState({showVideo: {from: pair.from, to: pair.to}});
        console.log(pair);
    },

    onRecallModeChange: function (e) {
        this.setState({recallMode: e.target.value});
    },
    onWorkDateChange: function (pair) {
        this.setState({workDate: {from: pair.from, to: pair.to}});
    },
    onAreaSelect: function (e) {
        if (!e.target.value) {
            this.setState({
                areaId: "",
                distributionHouseId: "",
                brId: "",
                distributionHouses: [],
                brs: []
            });
        }
        this.setState({areaId: e.target.value});
        this.updateDistributionHouses(e.target.value)
    },
    onDistributionHouseSelect: function (e) {
        if (!e.target.value) {
            this.setState({
                distributionHouseId: "",
                brId: "",
                brs: []
            });
        }
        this.setState({distributionHouseId: e.target.value});
        this.updateBrs(e.target.value)
    },
    onBrSelect: function (e) {
        if (!e.target.value) {
            this.setState({
                brId: ""
            });
        }
        this.setState({brId: e.target.value});
    },
    updateAreas: function () {
        var $this = this;
        $.ajax({
            url: $this.props.areaUrl,
            success: function (res) {
                $this.setState({areas: res.data});
            },
            error: function () {

            }
        });
    },
    updateDistributionHouses: function (areaId) {
        var $this = this;
        $.ajax({
            url: $this.props.distributionHouseUrl,
            data: {areaId: areaId},
            success: function (res) {
                $this.setState({distributionHouses: res.data});
            },
            error: function () {

            }
        });
    },
    updateBrs: function (distributionHouseId) {
        var $this = this;
        $.ajax({
            url: $this.props.brUrl,
            data: {distributionHouseId: distributionHouseId},
            success: function (res) {
                $this.setState({brs: res.data});
            },
            error: function () {

            }
        });
    },
    clear: function () {
        var state = copy(this.state);
        var initState = this.getInitialState();
        for (var x in state) {
            if (initState.hasOwnProperty(x)) {
                state[x] = initState[x]
            } else {
                state[x] = null
            }
        }
        state.areas = this.state.areas;
        this.setState(state);
    },
    updateFields: function (params) {
        var $this = this;
        $this.setState($this._updateFieldsWithParams(params));
    },
    _updateFieldsWithParams: function (params) {
        var $this = this;
        if (!!params.areaId) {
            $this.updateDistributionHouses(params.areaId)
        }
        if (!!params.distributionHouseId) {
            $this.updateBrs(params.distributionHouseId)
        }

        var pair = $this.splitRange(params["work-date-range"], ":");

        return {
            areaId: params.areaId,
            distributionHouseId: params.distributionHouseId,
            brId: params.brId,
            recallMode: params["recallMode"],
            workDate: {
                from: !pair['from'] ? "" : moment(pair['from'], "DD-MMM-YYYY").format("YYYY-MM-DD"),
                to: !pair['to'] ? "" : moment(pair['to'], "DD-MMM-YYYY").format("YYYY-MM-DD")
            },
            success: $this.splitRange(params["success-range"]),
            ptr: $this.splitRange(params["ptr-range"]),
            swp: $this.splitRange(params["swp-range"]),
            refreshment: $this.splitRange(params["refreshment-range"]),
            giveAway: $this.splitRange(params["give-away-range"]),
            showTools: $this.splitRange(params["show-tools-range"]),
            showVideo: $this.splitRange(params["show-video-range"]),
            packsell: $this.splitRange(params["pack-sell-range"]),
        };
    },
    splitRange: function (string, character) {
        if (!string) {
            return {from: "", to: ""};
        }
        character = character || "-";
        var splits = string.split(character, 2).map(function (val) {
            return val.trim();
        });
        return {from: splits[0], to: splits[1] || ""}
    }
});