import { createSlice, SliceCaseReducers, ValidateSliceCaseReducers } from "@reduxjs/toolkit";

export interface RecordState<T = any> {
  currentRecord: T;
  error: string;
  saveOnCreate: boolean;
  saveOnEdit: boolean;
}

export const createGenericSlice = <T, Reducers extends SliceCaseReducers<RecordState<T>>>({
  name = "",
  initialState,
  reducers,
}: {
  name: string;
  initialState: RecordState<T>;
  reducers: ValidateSliceCaseReducers<RecordState<T>, Reducers>;
}) => {
  return createSlice({
    name,
    initialState,
    reducers: {
      setCreateRecord(state, action) {
        state.saveOnCreate = action.payload;
      },
      setSaveOnEditRecord(state, action) {
        state.saveOnEdit = action.payload;
      },
      setCurrentRecord(state, action) {
        state.currentRecord = action.payload;
      },
      getError(state, action) {
        state.error = action.payload;
        state.currentRecord = null;
      },
      ...reducers,
    },
  });
};
