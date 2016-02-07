site.reactjs.grocery.FilterFields = React.createClass({
    getDefaultProps: function () {
        var baseUrl = 'http://182.160.99.116:6066';
        return {
            groceryUrl: baseUrl + '/contacts/groceries',
            locationUrl: baseUrl + '/contacts/locations',
            posNoUrl: baseUrl + '/contacts/posNos',
            dateMinMax: baseUrl + '/contacts/dateMinMax',
            formId: "",
            onFilterFiledsInit: function () {
            },
            scope: null,
        }
    },
    getInitialState: function () {

        var defaultState = {
            groceries: [],
            locations: [],
            posNos: [],
            date: {from: "", to: ""}
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
        $this.updateGroceries();
        $this.updateLocations();
        $this.updatePosNos();
        //$this.updateDateMinMax();
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
                <form id={$this.props.formId} style={{padding: '10px'}}>
                    <div className="row">

                        <div className="col-md-12">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Grocery Name</label>
                                <select className="form-control" value={$this.state.grocery}
                                        name="grocery"
                                        onChange={$this.onGrocerySelect}>
                                    <option value="">Select Grocery Name</option>
                                    {
                                        $this.state.groceries.map(function (grocery) {
                                            return (
                                                <option value={grocery} key={Math.random()}>{grocery}</option>
                                            );
                                        })
                                    }
                                </select>
                            </div>
                        </div>
                        <div className="col-md-12">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Location</label>
                                <select className="form-control" value={$this.state.location}
                                        name="location"
                                        onChange={$this.onLocationSelect}>
                                    <option value="">Select Location</option>
                                    {
                                        $this.state.locations.map(function (location) {
                                            return (
                                                <option value={location} key={Math.random()}>{location}</option>
                                            );
                                        })
                                    }
                                </select>
                            </div>
                        </div>
                        <div className="col-md-12">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Pos No</label>
                                <select className="form-control" name="posNo" value={$this.state.posNo}
                                        onChange={$this.onPosNoSelect}>
                                    <option value="">Select Pose No</option>
                                    {
                                        $this.state.posNos.map(function (poseNo) {
                                            return (
                                                <option value={poseNo} key={Math.random()}>{poseNo}</option>
                                            );
                                        })
                                    }
                                </select>
                            </div>
                        </div>

                        <div className="col-md-12">
                            <div className="form-group">
                                <label htmlFor="exampleInputEmail1">Date Range</label>
                                <DateRange modalId={Math.random()} modalTitle="Select Date Range"
                                           name="date"
                                           from={$this.state.date.from}
                                           to={$this.state.date.to} onChange={$this.onDateChange}/>
                            </div>
                        </div>

                    </div>
                </form>
            </div>
        );
    },
    onDateChange: function (pair) {
        this.setState({date: {from: pair.from, to: pair.to}});
    },
    onGrocerySelect: function (e) {
        if (!e.target.value) {
            this.setState({grocery: e.target.value});
            this.updateLocations();
            this.updatePosNos();
            return;
        }
        this.setState({grocery: e.target.value});
        this.updateLocations({grocery: e.target.value});
        this.updatePosNos({grocery: e.target.value});
    },
    onLocationSelect: function (e) {
        if (!e.target.value) {
            this.setState({location: e.target.value});
            this.updateGroceries();
            this.updatePosNos();
            return;
        }
        this.setState({location: e.target.value});
        this.updateGroceries({location: e.target.value});
        this.updatePosNos({location: e.target.value});
    },
    onPosNoSelect: function (e) {
        if (!e.target.value) {
            this.setState({posNo: e.target.value});
            this.updateGroceries();
            this.updateLocations();
            return;
        }
        this.setState({posNo: e.target.value});
        this.updateGroceries({posNo: e.target.value});
        this.updateLocations({posNo: e.target.value});
    },
    updateGroceries: function (data) {
        var $this = this;
        $.ajax({
            url: $this.props.groceryUrl,
            headers: {Authorization: 'kasj4asiuai45aiasdfk2adpas'},
            data: data,
            success: function (res) {
                $this.setState({groceries: res.data});
            },
            error: function () {

            }
        });
    },
    updateLocations: function (data) {
        var $this = this;
        $.ajax({
            url: $this.props.locationUrl,
            headers: {Authorization: 'kasj4asiuai45aiasdfk2adpas'},
            data: data,
            success: function (res) {
                $this.setState({locations: res.data});
            },
            error: function () {

            }
        });
    },
    updatePosNos: function (data) {
        var $this = this;
        $.ajax({
            url: $this.props.posNoUrl,
            headers: {Authorization: 'kasj4asiuai45aiasdfk2adpas'},
            data: data,
            success: function (res) {
                $this.setState({posNos: res.data});
            },
            error: function () {

            }
        });
    },
    updateDateMinMax: function (data) {
        var $this = this;
        $.ajax({
            url: $this.props.dateMinMax,
            headers: {Authorization: 'kasj4asiuai45aiasdfk2adpas'},
            data: data,
            success: function (js) {
                $this.setState({
                    date: {
                        from: !!$this.state.date.from ? $this.state.date.from : js['minDate'],
                        to: !!$this.state.date.to ? $this.state.date.to : js['maxDate']
                    }
                });
                console.log({
                    date: {
                        from: !!$this.state.date.from ? $this.state.date.from : js['minDate'],
                        to: !!$this.state.date.to ? $this.state.date.to : js['maxDate']
                    }
                });
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
        state.groceries = this.state.groceries;
        state.locations = this.state.locations;
        state.posNos = this.state.posNos;
        this.setState(state);
    },
    updateFields: function (params) {
        var $this = this;
        $this.setState($this._updateFieldsWithParams(params));
    },
    _updateFieldsWithParams: function (params) {
        var $this = this;
        var pp = {grocery: params.grocery, location: params.location, posNo: params.posNo}
        pp = removeEmptyNullWhiteSpaces(pp);
        $this.updatePosNos(exclude(pp, ['posNo']));
        $this.updateLocations(exclude(pp, ['location']));
        $this.updateGroceries(exclude(pp, ['grocery']));

        return {
            grocery: params.grocery,
            location: params.location,
            posNo: params.posNo,
            date: $this.splitRange(params["date"], ":")
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

        return {
            from: !splits[0] ? "" : moment(splits[0], "DD-MMM-YYYY").format("YYYY-MM-DD"),
            to: !splits[1] ? "" : moment(splits[1], "DD-MMM-YYYY").format("YYYY-MM-DD") || ""
        }
    },


});