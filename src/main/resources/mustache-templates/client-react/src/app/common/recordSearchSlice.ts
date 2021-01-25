import { createSlice, SliceCaseReducers, ValidateSliceCaseReducers } from "@reduxjs/toolkit";

export interface SearchState<RECORD = any, TEMPLATE = any> {
  searchTemplate?: TEMPLATE;
  error?: string;
  isLoading?: boolean;
  searchId?: string;
  searchResult?: Array<RECORD>;
  resultSetSize?: number;
  pageSize?: number;
  page?: number;
  submit?: boolean;
}

export const createGenericSearchSlice = <
  RECORD,
  TEMPLATE,
  Reducers extends SliceCaseReducers<SearchState<RECORD, TEMPLATE>>
>({
  name = "",
  initialState,
  reducers,
}: {
  name: string;
  initialState: SearchState<RECORD, TEMPLATE>;
  reducers: ValidateSliceCaseReducers<SearchState<RECORD, TEMPLATE>, Reducers>;
}) => {
  return createSlice({
    name,
    initialState,
    reducers: {
      setSearchTemplate(state, action) {
        state.searchTemplate = action.payload;
      },
      setResultSetSize(state, action){
        state.resultSetSize = action.payload;
      },
      setSearchId(state, action) {
        state.searchId = action.payload;
      },
      searchError(state, action) {
        state.error = action.payload;
        state.searchTemplate = null;
        state.searchResult = [];
      },
      isLoading(state, action) {
        state.isLoading = action.payload;
      },
      searchSuccess(state, action) {
        state.searchResult = action.payload;
      },
      submitSearch(state, action) {
        state.submit = action.payload;
      },
      ...reducers,
    },
  });
};
