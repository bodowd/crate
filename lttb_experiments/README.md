This contains some experiments for the largest triangle three buckets aggregation in CrateDB.

To run the experiment in the jupyter notebook, you need to install [uv](https://docs.astral.sh/uv/getting-started/installation/)
and have a local CrateDB node running at port 4200 ([instructions to run Crate locally](https://github.com/crate/crate/blob/master/devs/docs/basics.rst#manual-build))

Then create a virtual env with
```shell
uv venv
```

Activate the venv with
```shell
source .venv/bin/activate
```

Install dependencies with
```shell
uv sync
```

Run the notebook with
```shell
uv run --with jupyter jupyter lab
```

Your browser should then open to `localhost:8888` and then you can run the code in `udf_vs_native_lttb.ipynb`
