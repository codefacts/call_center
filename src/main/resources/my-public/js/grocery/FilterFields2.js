site.reactjs.FilterFields2 = React.createClass({
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
        var defaultState = this.defaultState();
        if (!!this.props.scope && !!this.props.scope.get()) {
            return this.props.scope.get();
        }
        else {
            return merge(defaultState, {
                areas: [],
                distributionHouses: [],
                brs: []
            });
        }
    },
    defaultState: function () {
        return {
            areaId: "",
            distributionHouseId: "",
            brId: "",
            workDate: "",
            ptr: "",
            swp: "",
            refreshment: "",
            giveAway: "",
            showTools: "",
            showVideo: "",
            packsell: "",
        };
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
                                                <option value={area.id} key={Math.random()}>{area.name}</option>
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
                                                <option value={area.id} key={Math.random()}>{area.name}</option>
                                            );
                                        })
                                    }
                                </select>
                            </div>
                        </div>

                        <div className="col-md-12">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Work Date</label>
                                <DatePickerPopup modalId={Math.random()} modalTitle="Select Date Range"
                                                 name="workDate" value={$this.state.workDate}
                                                 onChange={$this.onWorkDateChange}/>
                            </div>
                        </div>

                        <div className="col-md-4">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">PTR</label>
                                <select className="form-control" name="ptr" value={$this.state.ptr}
                                        onChange={$this.onPtrChange}>
                                    <option value="">Select PTR</option>
                                    <option value="1">Yes</option>
                                    <option value="0">No</option>
                                </select>
                            </div>
                        </div>
                        <div className="col-md-4">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">SWP</label>
                                <select className="form-control" name="swp" value={$this.state.swp}
                                        onChange={$this.onSwpChange}>
                                    <option value="">Select SWP</option>
                                    <option value="1">Yes</option>
                                    <option value="0">No</option>
                                </select>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Refreshment</label>
                                <select className="form-control" name="refreshment" value={$this.state.refreshment}
                                        onChange={$this.onRefreshmentChange}>
                                    <option value="">Select Refreshment</option>
                                    <option value="1">Yes</option>
                                    <option value="0">No</option>
                                </select>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Give Away</label>
                                <select className="form-control" name="giveAway" value={$this.state.giveAway}
                                        onChange={$this.onGiveAwayChange}>
                                    <option value="">Select Give Away</option>
                                    <option value="1">Yes</option>
                                    <option value="0">No</option>
                                </select>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Pack Sell</label>
                                <select className="form-control" name="packsell" value={$this.state.packsell}
                                        onChange={$this.onPacksellChange}>
                                    <option value="">Select Pack Sell</option>
                                    <option value="1">Yes</option>
                                    <option value="0">No</option>
                                </select>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Show Tools</label>
                                <select className="form-control" name="showTools" value={$this.state.showTools}
                                        onChange={$this.onShowToolsChange}>
                                    <option value="">Select Show Tools</option>
                                    <option value="1">Yes</option>
                                    <option value="0">No</option>
                                </select>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Show Video</label>
                                <select className="form-control" name="showVideo" value={$this.state.showVideo}
                                        onChange={$this.onShowVideoChange}>
                                    <option value="">Select Show Video</option>
                                    <option value="1">Yes</option>
                                    <option value="0">No</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        );
    },
    onPtrChange: function (e) {
        this.setState({ptr: e.target.value});
    },
    onSwpChange: function (pair) {
        this.setState({swp: pair.target.value});
        console.log(pair);
    },
    onPacksellChange: function (pair) {
        this.setState({packsell: pair.target.value});
        console.log(pair);
    },
    onRefreshmentChange: function (pair) {
        this.setState({refreshment: pair.target.value});
        console.log(pair);
    },
    onGiveAwayChange: function (pair) {
        this.setState({giveAway: pair.target.value});
        console.log(pair);
    },
    onShowToolsChange: function (pair) {
        this.setState({showTools: pair.target.value});
        console.log(pair);
    },
    onShowVideoChange: function (pair) {
        this.setState({showVideo: pair.target.value});
        console.log(pair);
    },
    onWorkDateChange: function (date) {
        console.log("WORK DATE CHANGED: " + date);
        this.setState({workDate: date});
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

        return merge(params, $this.defaultState());
    }
});